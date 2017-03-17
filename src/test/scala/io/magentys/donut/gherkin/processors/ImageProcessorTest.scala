package io.magentys.donut.gherkin.processors

import io.magentys.donut.DonutTestData
import io.magentys.donut.gherkin.model.Embedding
import org.scalatest.{FlatSpec, Matchers}

class ImageProcessorTest extends FlatSpec with Matchers {

  ImageProcessor.imageMap = scala.collection.mutable.Map[Int, Embedding]()

  val features = DonutTestData.features_sample_2.right.get;

  behavior of "ImageProcessor"

  it should "calculate the hash id for each screenshot per scenario " in {
    val tmp_scenario = features.flatMap(f => f.scenarios)
    val embeddings = List(DonutTestData.embedding)
    ImageProcessor.getScreenshotIds(embeddings) shouldBe "578984356"
//    ImageProcessor.imageMap.keys shouldBe Set(578984356)
    ImageProcessor.imageMap(578984356).mime_type shouldBe "image/png"
  }
}

