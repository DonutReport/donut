package report.donut

import org.scalatest.{FlatSpec, Matchers}

class GeneratorTest extends FlatSpec with Matchers {

  behavior of "Generator"

  it should "throw an exception if source paths are not provided" in {
    val sourcePaths = ""
    val exception = intercept[DonutException] {
      Generator.createReport(sourcePaths, projectName = "", projectVersion = "")
    }
    assert(exception.mgs === "Unable to extract the paths to the result sources. Please use this format:- gherkin:/my/path/cucumber-reports,/my/other/path/cucumber-reports")
  }

  it should "return report if valid gherkin json files are found" in {
    Generator.createReport("gherkin:src/test/resources/mix-gherkin-2-and-5", projectName = "", projectVersion = "") match {
      case Left(e) => fail(e)
      case Right(r) =>
        r should not be null
    }
  }

  it should "return report if valid gherkin and adapted unit json files are found" in {
    Generator.createReport("gherkin:src/test/resources/cuke-and-unit/cuke,gherkin:src/test/resources/cuke-and-unit/unit", projectName = "", projectVersion = "") match {
      case Left(e) => fail(e)
      case Right(r) =>
        r should not be null
    }
  }

  it should "return report if only adapted unit json files are provided" in {
    Generator.createReport("src/test/resources/cuke-and-unit/unit", projectName = "", projectVersion = "") match {
      case Left(e) => fail(e)
      case Right(r) =>
        r should not be null
    }
  }
}
