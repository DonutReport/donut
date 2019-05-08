package report.donut

import java.io.File

import org.apache.commons.lang3.StringUtils
import org.json4s.{DefaultFormats, JValue}
import report.donut.gherkin.processors.JSONProcessor
import report.donut.transformers.cucumber.Feature

import scala.util.Try

trait ResultLoader {
  def load(): Either[String, List[Feature]]
}

object ResultLoader {

  private[donut] class CucumberResultLoader(sourceDir: File) extends ResultLoader {
    override def load(): Either[String, List[Feature]] = {
      if (!sourceDir.exists) {
        return Left(s"Source directory does not exist: $sourceDir")
      }

      val jsonValues = JSONProcessor.loadFrom(sourceDir) match {
        case Left(errors) => return Left(errors)
        case Right(r) => if (r.isEmpty) return Left("No files found of correct format") else Right(r)
      }

      Try(loadCukeFeatures(jsonValues.right.get)).toEither(_.getMessage)
    }

    private[donut] def loadCukeFeatures(json: List[JValue]) = {
      implicit val formats = DefaultFormats
      json.flatMap(f => f.extract[List[Feature]])
    }
  }

  def apply(resultSource: String): ResultLoader = {
    val pattern = "([a-zA-z]{2,}):(.*)".r
    pattern.findFirstMatchIn(resultSource) match {
      case Some(m) => {
        val format = m.group(1)
        val sourcePath = m.group(2)
        if (StringUtils.isBlank(sourcePath)) {
          throw new DonutException("Please provide the source directory path.")
        }
        format match {
          case "cucumber" => new CucumberResultLoader(new File(sourcePath))
          case _ => throw DonutException(s"Unsupported result format: $format")
        }
      }
      case None => new CucumberResultLoader(new File(resultSource)) //Defaults to cucumber result format
    }
  }
}
