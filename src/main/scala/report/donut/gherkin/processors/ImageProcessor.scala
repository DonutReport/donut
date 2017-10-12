package report.donut.gherkin.processors

import report.donut.gherkin.model.Embedding
import org.json4s.jackson.Serialization
import org.json4s.{Formats, NoTypeHints, jackson}

object ImageProcessor {

  var imageMap = scala.collection.mutable.Map[Int, Embedding]()

  def getScreenshotIds(embeddings: List[Embedding]): String = {
    val a: Map[Int, Embedding] = embeddings.map(e => (e.data.hashCode -> e)).toMap
    a.map(a => imageMap += a)
    a.map(a => a._1).toList.mkString(",")
  }

  def allImages: String = {
    implicit def json4sJacksonFormats: Formats = jackson.Serialization.formats(NoTypeHints)
    val b: List[Embedding] = imageMap.map { case (k, v) => new Embedding(v.mime_type, v.data, k) }.toList
    Serialization.writePretty(b)
  }

}