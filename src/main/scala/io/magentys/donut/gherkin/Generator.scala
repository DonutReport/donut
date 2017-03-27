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
      case Right(report) => ReportConsole(report)
      case Left(error) => throw DonutException(s"An error occurred while generating donut report. $error")
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
                                    customAttributes: Map[String, String] = Map()): Either[String, Report] = {

    //Prepare objects
    val statusConf = StatusConfiguration(countSkippedAsFailure, countPendingAsFailure, countUndefinedAsFailure, countMissingAsFailure)
    val projectMetadata = ProjectMetadata(projectName, projectVersion, customAttributes)
    val reportStartedTimestamp = Try(formatter.parseDateTime(datetime)).getOrElse(DateTime.now)
    val cukeSourceDir = new File(getCukePath(sourcePaths))

    for {
      folder      <- if (cukeSourceDir.exists()) Right(cukeSourceDir).right else Left("Source directory doesn't exist").right
      jsons       <- JSONProcessor.loadFrom(folder).right
      _           <- if (jsons.isEmpty) Left("No files found of correct format").right else Right(jsons).right
      features    <- timed("step1", "Loaded JSON files") {loadDonutFeatures(cukeSourceDir, getNonCukePaths(sourcePaths), statusConf).right}
      report      <- timed("step3", "Produced report") {Right(Report(features.toList, reportStartedTimestamp, projectMetadata)).right}
      _           <- TemplateEngine(report, s"/templates/$template/index.html").renderToHTML(outputPath, filePrefix).right
    } yield report
  }

  /**
  Loads donut features for both cuke and non cuke json files.<br>
  Currently, it works only for one non-cuke path and it's tested only for NUnit results(json converted from xml using our adapter).<br>
  It would be enhanced soon to report other types of non-cuke reports and also to publish more than 1 type of non-cuke results in one report.
  */
  def loadDonutFeatures(cukeSourceDir: File, nonCukePaths: List[String], statusConf: StatusConfiguration): Either[String, ListBuffer[model.Feature]] = {
    var donutFeatures = new ListBuffer[model.Feature]
    val cukeJsonValues = JSONProcessor.loadFrom(cukeSourceDir).right.get

    donutFeatures = CucumberTransformer.transform(cukeJsonValues, donutFeatures, statusConf).right.get
    if (nonCukePaths.nonEmpty) {
      val nonCukeSourceDir = new File(nonCukePaths.head)
      if (nonCukeSourceDir.exists()) {
        val jsonValues = JSONProcessor.loadFrom(nonCukeSourceDir)
        donutFeatures = CucumberTransformer.transform(jsonValues.right.get, donutFeatures, statusConf).right.get
      }
    }
    Try(donutFeatures).toEither(_.getMessage)
  }

  private[gherkin] def getCukePath(sourcePaths: String): String = {
    log.info("source paths: " + sourcePaths)
    val paths = sourcePaths.split(",")
    for (path <- paths) {
      if (StringUtils.containsIgnoreCase(path, "cucumber:") || StringUtils.containsIgnoreCase(path, "specflow:")) {
        val pattern = "(cucumber:|specflow:)(.*)".r
        val pattern(identifier,cukePath) = path

        if(StringUtils.isNotBlank(cukePath)){
          log.info("Cuke path: " + cukePath)
          return cukePath
        }else{
          throw DonutException("Please provide the source directory path.")
        }
      }
    }
    throw DonutException("Unable to extract the path to cucumber/specflow reports. Please use this format:- cucumber:/my/path/cucumber-reports,/my/path/nunit-reports")
  }

  private[gherkin] def getNonCukePaths(sourcePaths: String) = {
    val nonCukePaths = new ListBuffer[String]
    val paths = sourcePaths.split(",")
    for (path <- paths) {
      if (!(StringUtils.containsIgnoreCase(path, "cucumber:") || StringUtils.containsIgnoreCase(path, "specflow:"))) {
        nonCukePaths += path
      }
    }
    nonCukePaths.toList
  }

}

case class DonutException(mgs: String) extends Exception
