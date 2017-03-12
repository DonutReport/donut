package io.magentys.donut.gherkin

import java.io.File

import io.magentys.donut.gherkin.model._
import io.magentys.donut.gherkin.processors.JSONProcessor
import io.magentys.donut.log.Log
import io.magentys.donut.performance.PerformanceSupport
import io.magentys.donut.template.TemplateEngine
import io.magentys.donut.transformers.cucumber.CucumberTransformer
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

import scala.collection.mutable.ListBuffer
import scala.util.Try

object Generator extends Log with PerformanceSupport {

  val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd-HHmm")

  //this wrapper is currently used to help the java maven plugin
  def apply(sourcePaths: String,
            outputPath: String = "donut",
            filePrefix: String = "",
            dateTime: String,
            template: String = "default",
            countSkippedAsFailure: Boolean = false,
            countPendingAsFailure: Boolean = false,
            countUndefinedAsFailure: Boolean = false,
            countMissingAsFailure: Boolean = false,
            projectName: String,
            projectVersion: String,
            customAttributes: scala.collection.mutable.Map[String, String]): ReportConsole = {

    createReport(sourcePaths, outputPath, filePrefix, dateTime, template, countSkippedAsFailure, countPendingAsFailure,
      countUndefinedAsFailure, countMissingAsFailure, projectName, projectVersion, customAttributes.toMap) match {
      case Some(report) => ReportConsole(report)
      case None => throw DonutException("An error occurred while generating donut report.")
    }
  }

  private[gherkin] def createReport(sourcePaths: String,
                                    outputPath: String = "donut",
                                    filePrefix: String = "",
                                    datetime: String = formatter.print(DateTime.now),
                                    template: String = "default",
                                    countSkippedAsFailure: Boolean = false,
                                    countPendingAsFailure: Boolean = false,
                                    countUndefinedAsFailure: Boolean = false,
                                    countMissingAsFailure: Boolean = false,
                                    projectName: String,
                                    projectVersion: String,
                                    customAttributes: Map[String, String] = Map()): Option[Report] = {

    //Prepare objects
    val statusConf = StatusConfiguration(countSkippedAsFailure, countPendingAsFailure, countUndefinedAsFailure, countMissingAsFailure)
    val projectMetadata = ProjectMetadata(projectName, projectVersion, customAttributes)
    val reportStartedTimestamp = Try(formatter.parseDateTime(datetime)).getOrElse(DateTime.now)
    val sourceDir = new File(getCukePath(sourcePaths))

    if (sourceDir.exists) {

      //Step 1: Load BDD & non-BDD json files
      val donutFeatures = loadDonutFeatures(sourceDir, getNonCukePaths(sourcePaths), statusConf)
      if (donutFeatures.nonEmpty) {

        //Step 2: Main Engine - do calculations and produce the report object to bind
        val report: Report = timed("step3", "Produced report") {
          Report(donutFeatures.toList, reportStartedTimestamp, projectMetadata)
        }

        //Step 3: Bind and render the final result
        timed("step4", "Rendered report to html") {
          TemplateEngine(report, s"/templates/$template/index.html").renderToHTML(outputPath, filePrefix)
        }

        Some(report)

      } else {
        log.error(s"No feature reports found at: $sourceDir")
        None
      }
    } else {
      log.error(s"The source directory: $sourceDir does not exist")
      None
    }
  }

  def loadDonutFeatures(sourceDir: File, nonCukePaths: List[String], statusConf: StatusConfiguration): ListBuffer[model.Feature] = {
    var donutFeatures = new ListBuffer[model.Feature]

    //Step 1: load json files from BDD feature files(cucumber/specFlow) dir
    donutFeatures = timed("step1", "Loaded JSON files") {
      val jsonValues = JSONProcessor.loadFrom(sourceDir)
      CucumberTransformer.transform(jsonValues, donutFeatures, statusConf)
    }

    //Step 2: load json files from non-BDD test sources(ex: Unit Tests) dir
    if (nonCukePaths.size > 1) {
      val nonCukeSourceDir = new File(nonCukePaths(1))

      if (nonCukeSourceDir.exists()) {
        donutFeatures = timed("step2", "Loaded non-BDD JSON files") {
          val jsonValues = JSONProcessor.loadFrom(nonCukeSourceDir)
          CucumberTransformer.transform(jsonValues, donutFeatures, statusConf)
        }
      }
    }
    donutFeatures
  }

  private[gherkin] def getCukePath(sourcePaths: String): String = {
    log.info("source paths: " + sourcePaths)
    val paths = sourcePaths.split(",")
    for (path <- paths) {
      if (StringUtils.containsIgnoreCase(path, "cucumber:") || StringUtils.containsIgnoreCase(path, "specflow:")) {
        val parts = path.split(":")
        if (parts.length > 1) {
          log.info("Cuke path: " + parts(1))
          return parts(1)
        }
      }
    }
    throw DonutException("Unable to extract the path to cucumber/specflow reports. Please use this format:- cucumber:/my/path/cucumber-reports,/my/path/nunit-reports")
  }

  private[gherkin] def getNonCukePaths(sourcePaths: String) = {
    val nonCukePaths = new ListBuffer[String]
    val paths = sourcePaths.split(",")
    for (path <- paths) {
      if (!(StringUtils.containsIgnoreCase(path, "cucumber") || StringUtils.containsIgnoreCase(path, "specflow"))) {
        nonCukePaths += path
      }
    }
    nonCukePaths.toList
  }

}

case class DonutException(mgs: String) extends Exception
