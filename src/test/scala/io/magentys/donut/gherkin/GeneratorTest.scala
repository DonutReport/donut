package io.magentys.donut.gherkin

import org.scalatest.{FlatSpec, Matchers}

class GeneratorTest  extends FlatSpec with Matchers {

  behavior of "Generator"

  it should "return None if source directory does not exist" in {
    Generator.createReport("", projectName = "", projectVersion = "") shouldBe None
  }

  it should "return None if json files not found" in {
    Generator.createReport("src/test/resources/samples-empty", projectName = "", projectVersion = "") shouldBe None
  }

  it should "return report if valid json files are found" in {
    Generator.createReport("src/test/resources/samples-1", projectName = "", projectVersion = "") should not be None
  }

}