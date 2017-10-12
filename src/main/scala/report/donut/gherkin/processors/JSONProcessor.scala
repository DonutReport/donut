package report.donut.gherkin.processors

import java.io.File

import report.donut.log.Log
import org.json4s.{DefaultFormats, JValue}
import org.json4s.jackson.JsonMethods._
import scala.util.Try
import report.donut._
object JSONProcessor extends Log {

  /**
    * Gets all file names that represent a report.
    * Excludes empty files based on size.
    * @param directory The root directory for the reports
    */
  def loadFrom(directory: File):  Either[String, List[JValue]] = {
    log.debug("Loading files from directory: " + directory)
    sequenceEither(getValidFiles(directory).map(parseJsonFile)) match {
      case Left(errors) => Left(errors.foldLeft("")(_ + _ + ","))
      case Right(r) => Right(r)
    }
  }

  /**
    * Parses a json file and produces a list of Feature.
    * @param jsonPath the file canonical path
    * @return list of Feature per file
    */
  def parseJsonFile(jsonPath: String): Either[String, JValue] = {
    log.debug("Parsing file: " + jsonPath)
    val json = scala.io.Source.fromFile(jsonPath).mkString
    implicit val formats = DefaultFormats
    Try(parse(json, useBigDecimalForDouble = true)).toEither(_ => s"Json could not be parsed for $jsonPath")
  }

  /**
    * finds only valid files
    * @param directory
    * @return
    */
  def getValidFiles(directory: File): List[String] = {
    val allFiles = directory.listFiles
    log.debug("Total files found: " + allFiles.size)

    val validJsonFiles: Array[String] = allFiles
      .filter(f => f.getName.endsWith(".json"))
      .filter(f => f.length > 2)
      .map(g => g.getPath)

    log.debug("Files valid to parse: " + validJsonFiles.length)

    validJsonFiles.toList
  }
}