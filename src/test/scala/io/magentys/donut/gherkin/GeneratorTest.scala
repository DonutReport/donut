package io.magentys.donut.gherkin

import io.magentys.donut.DonutTestData
import org.scalatest.{FlatSpec, Matchers}

class GeneratorTest  extends FlatSpec with Matchers {

  behavior of "Generator"

  val features = DonutTestData.features_sample_2;

  it should "return None if source directory does not exist" in {
    Generator.createReport("", projectName = "", projectVersion = "") shouldBe None
  }

  it should "return None if json files not found" in {
    Generator.createReport("src/test/resources/samples-empty", projectName = "", projectVersion = "") shouldBe None
  }
}