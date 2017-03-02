package io.magentys.donut.gherkin

import org.scalatest.{FlatSpec, Matchers}

class GeneratorTest  extends FlatSpec with Matchers {

  behavior of "Generator"

  it should "return None if source directory does not exist" in {
    Generator.createReport("", projectName = "", projectVersion = "") shouldBe Left("Source directory doesn't exist")
  }

  it should "should return error if no cucumber features" in {
    Generator.createReport("src/test/resources/samples-empty", projectName = "", projectVersion = "") shouldBe Left("No files found of correct format")
  }

  it should "should return error if it tries to parse an invalid file" in {
    Generator.createReport("src/test/resources/samples-weirdos", projectName = "", projectVersion = "") shouldBe Left("Json could not be parsed for src/test/resources/samples-weirdos/invalid_format.json,")
  }
}