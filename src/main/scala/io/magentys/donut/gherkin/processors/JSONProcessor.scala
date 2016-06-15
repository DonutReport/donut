package io.magentys.donut.gherkin.processors

import java.io.File
import io.magentys.donut.log.Log
import org.json4s.{JValue, DefaultFormats}
import org.json4s.jackson.JsonMethods._

import scala.util.Try

object JSONProcessor extends Log {

  /**
    * Gets all file names that represent a report.
    * Excludes empty files based on size.
    * @param directory The root directory for the reports
    */
  def loadFrom(directory: File): List[JValue] = {
    log.debug("Loading files from directory: " + directory)
    convertToFeatures(getValidFiles(directory)).flatten
  }

  /**
    * All json files are parsed in a List of Feature
    * @return list of Feature
    */
  private[processors] def convertToFeatures(jsonFiles: List[String]) = {
    log.debug("Loading files...")
    if(jsonFiles == None) List.empty
    else jsonFiles.map(jsonPath => parseJsonFile(jsonPath))
  }


  /**
    * Parses a json file and produces a list of Feature.
    * @param jsonPath the file canonical path
    * @return list of Feature per file
    */
  def parseJsonFile(jsonPath: String): Option[JValue] = {
    log.debug("Parsing file: " + jsonPath)
    val json = scala.io.Source.fromFile(jsonPath).mkString
    implicit val formats = DefaultFormats
    Try(parse(json, useBigDecimalForDouble = true)).toOption
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