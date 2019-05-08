package report.donut

import org.scalatest.{FlatSpec, Matchers}

class GeneratorTest extends FlatSpec with Matchers {

  behavior of "Generator"

  it should "throw an exception if source paths are not provided" in {
    val sourcePaths = ""
    val exception = intercept[DonutException] {
      Generator.createReport(sourcePaths, projectName = "", projectVersion = "")
    }
    assert(exception.mgs === "Unable to extract the paths to the result sources. Please use this format:- cucumber:/my/path/cucumber-reports,cucumber:/my/other/path/adapted-reports")
  }

  it should "return report if valid cucumber json files are found" in {
    Generator.createReport("cucumber:src/test/resources/mix-cucumber-1-and-4", projectName = "", projectVersion = "") match {
      case Left(e) => fail(e)
      case Right(r) =>
        r should not be null
    }
  }

  it should "return report if valid cucumber and adapted unit json files are found" in {
    Generator.createReport("cucumber:src/test/resources/cuke-and-unit/cuke,cucumber:src/test/resources/cuke-and-unit/unit", projectName = "", projectVersion = "") match {
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
