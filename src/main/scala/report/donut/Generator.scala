package report.donut

import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import report.donut.gherkin.model._
import report.donut.transformers.cucumber.{Feature => CucumberFeature}
import report.donut.log.Log
import report.donut.performance.PerformanceSupport
import report.donut.template.TemplateEngine
import report.donut.transformers.cucumber.CucumberTransformer

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

  private[donut] def createReport(sourcePaths: String,
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

    for {
      features <- timed("step1", "Loaded JSON files") { loadResultSources(sourcePaths, statusConf).right }
      report <- timed("step2", "Produced report") { Right(Report(features, reportStartedTimestamp, projectMetadata)).right }
      _ <- TemplateEngine(report, s"/templates/$template/index.html").renderToHTML(outputPath, filePrefix).right
    } yield report
  }

  /**
    * Currently loads the result sources for gherkin result JSON files only.<br>
    * This can include result sources that have been adapted to a gherkin result format from either JUnit or NUnit formats.
    * Any number of result source paths can be specified using a comma delimiter.
    *
    * @param sourcePaths the paths to the result sources
    * @param statusConf  the status configuration to consider as a failure
    * @return either an error message or a list of Features
    */
  def loadResultSources(sourcePaths: String, statusConf: StatusConfiguration): Either[String, List[Feature]] = {
    log.info("source paths: " + sourcePaths)
    if (StringUtils.isBlank(sourcePaths)) {
      throw DonutException("Unable to extract the paths to the result sources. Please use this format:- gherkin:/my/path/cucumber-reports,/my/other/path/cucumber-reports")
    }
    var features = new ListBuffer[CucumberFeature]
    for (path <- sourcePaths.split(",")) {
      val loader = ResultLoader(path)
      features ++= loader.load.right.get
    }
    val donutFeatures = CucumberTransformer.transform(features.toList, statusConf).right.get
    Try(donutFeatures.toList).toEither(_.getMessage)
  }
}

case class DonutException(mgs: String) extends Exception
