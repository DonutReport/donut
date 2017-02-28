package io.magentys.donut.gherkin

import io.magentys.donut.DonutTestData
import org.scalatest.{FlatSpec, Matchers}
import scalaz.\/.{left, right}

class GeneratorTest  extends FlatSpec with Matchers {

  behavior of "Generator"

  it should "return None if source directory does not exist" in {
    Generator.createReport("", projectName = "", projectVersion = "") shouldBe left("Source directory doesn't exist")
  }

  it should "should return error if no cucumber features" in {
    Generator.createReport("src/test/resources/samples-empty", projectName = "", projectVersion = "") shouldBe left("No cucumber reports found")
  }

  it should "should return error if it tries to parse an invalid file" in {
    Generator.createReport("src/test/resources/samples-weirdos", projectName = "", projectVersion = "") shouldBe left("No cucumber  found")
  }
}