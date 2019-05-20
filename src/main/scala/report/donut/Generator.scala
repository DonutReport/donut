package report.donut

import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import report.donut.gherkin.model._
import report.donut.log.Log
import report.donut.performance.PerformanceSupport
import report.donut.template.TemplateEngine
import report.donut.transformers.cucumber.{CucumberTransformer, Feature => CucumberFeature}

import scala.collection.mutable.ListBuffer
import scala.util.Try

object Generator extends Log with PerformanceSupport {

  val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd-HHmm")

  //this wrapper is currently used to help the java maven plugin
  def apply(resultSources: String,
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

    createReport(resultSources, outputPath, filePrefix, dateTime, template, countSkippedAsFailure, countPendingAsFailure,
      countUndefinedAsFailure, countMissingAsFailure, projectName, projectVersion, customAttributes.toMap) match {
      case Right(report) => ReportConsole(report)
      case Left(error) => throw DonutException(s"An error occurred while generating donut report. $error")
    }
  }

  private[donut] def createReport(resultSources: String,
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
      resultSourceList <- if (!StringUtils.isBlank(resultSources)) Right(resultSources.split(",").map(_.trim).toList).right else Left("Unable to extract the paths to the result sources. Please use this format:- cucumber:/my/path/cucumber-reports,cucumber:/my/other/path/adapted-reports").right
      features <- timed("step1", "Loaded result sources") {
        loadResultSources(resultSourceList, statusConf).right
      }
      report <- timed("step2", "Produced report") {
        Right(Report(features, reportStartedTimestamp, projectMetadata)).right
      }
      _ <- TemplateEngine(report, s"/templates/$template/index.html").renderToHTML(outputPath, filePrefix).right
    } yield report
  }

  /**
    * Currently loads result sources for cucumber result JSON files only.<br>
    * This can include result sources that have been adapted to a cucumber result format from either JUnit or NUnit formats.
    * Any number of result sources can be specified using a comma delimiter. The result sources include a format and path delimited by a colon,
    * e.g. cucumber:/path/to/results
    *
    * @param resultSourceList a list of result sources
    * @param statusConf       the status configuration to consider as a failure
    * @return either an error message or a list of Features
    */
  def loadResultSources(resultSourceList: List[String], statusConf: StatusConfiguration): Either[String, List[Feature]] = {
    var features = new ListBuffer[CucumberFeature]
    for (resultSource <- resultSourceList) {
      val result = ResultLoader(resultSource).load
      if (result.isLeft) return Left(result.left.get)
      features ++= result.right.get
    }
    val donutFeatures = CucumberTransformer.transform(features.toList, statusConf).right.get
    Try(donutFeatures.toList).toEither(_.getMessage)
  }
}

case class DonutException(mgs: String) extends Exception
