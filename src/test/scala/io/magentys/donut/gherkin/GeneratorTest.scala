package io.magentys.donut.gherkin

import org.scalatest.{FlatSpec, Matchers}

class GeneratorTest extends FlatSpec with Matchers {

  behavior of "Generator"

  it should "throw an exception if cucumber/specflow source directory is not provided" in {
    val sourcePaths = ""
    val exception = intercept[DonutException] {
      Generator.createReport(sourcePaths, projectName = "", projectVersion = "")
    }
    assert(exception.mgs === "Unable to extract the path to cucumber/specflow reports. Please use this format:- cucumber:/my/path/cucumber-reports,/my/path/nunit-reports")
  }

  it should "throw an exception if cucumber/specflow source directory is not specified with correct identier 'cucumber:' or 'specflow'" in {
    val sourcePaths = "src/test/resources/samples-1"
    val exception = intercept[DonutException] {
      Generator.createReport(sourcePaths, projectName = "", projectVersion = "")
    }
    assert(exception.mgs === "Unable to extract the path to cucumber/specflow reports. Please use this format:- cucumber:/my/path/cucumber-reports,/my/path/nunit-reports")
  }

  it should "return None if cucumber/specflow source directory does not exist" in {
    Generator.createReport("cucumber:src/test/resources/non-existing-dir", projectName = "", projectVersion = "") shouldBe None
  }

  it should "return None if json files not found" in {
    Generator.createReport("cucumber:src/test/resources/samples-empty", projectName = "", projectVersion = "") shouldBe None
  }

  it should "return report if valid json files are found" in {
    Generator.createReport("cucumber:src/test/resources/samples-1", projectName = "", projectVersion = "") should not be None
  }

  behavior of "Generator Units"

  it should "return correct cuke source path if the cucumber source paths format is correct" in {
    val sourcePaths = "cucumber:/my/path/cucumber-reports,/my/path/nunit-reports"
    val bddSourcePath = Generator.getCukePath(sourcePaths)
    bddSourcePath shouldBe "/my/path/cucumber-reports"
  }

  it should "return correct cuke source path if the specflow source paths format is correct" in {
    val sourcePaths = "specflow:/my/path/cucumber-reports,/my/path/nunit-reports"
    val bddSourcePath = Generator.getCukePath(sourcePaths)
    bddSourcePath shouldBe "/my/path/cucumber-reports"
  }

  it should "throw a donut exception if the cucumber identifier is provided without the path" in {
    val sourcePaths = "cucumber:"
    val exception = intercept[DonutException] {
      Generator.getCukePath(sourcePaths)
    }
    assert(exception.mgs === "Unable to extract the path to cucumber/specflow reports. Please use this format:- cucumber:/my/path/cucumber-reports,/my/path/nunit-reports")
  }

  it should "throw a donut exception if the cucumber source path format is incorrect" in {
    val sourcePaths = "cucumber;/my/path/cucumber-reports,/my/path/nunit-reports"
    val exception = intercept[DonutException] {
      Generator.getCukePath(sourcePaths)
    }
    assert(exception.mgs === "Unable to extract the path to cucumber/specflow reports. Please use this format:- cucumber:/my/path/cucumber-reports,/my/path/nunit-reports")
  }

  it should "throw a donut exception if the specflow source path format is incorrect" in {
    val sourcePaths = "specflow;/my/path/cucumber-reports,/my/path/nunit-reports"
    val exception = intercept[DonutException] {
      Generator.getCukePath(sourcePaths)
    }
    assert(exception.mgs === "Unable to extract the path to cucumber/specflow reports. Please use this format:- cucumber:/my/path/cucumber-reports,/my/path/nunit-reports")
  }

  it should "return correct non cuke source path if the non cuke source paths are provided" in {
    val sourcePaths = "cucumber:/my/path/cucumber-reports,/my/path/nunit-reports"
    val nonCukePaths = Generator.getNonCukePaths(sourcePaths)
    nonCukePaths.size shouldBe 1
    nonCukePaths.head shouldBe "/my/path/nunit-reports"
  }

  it should "return empty list if the non cuke source paths are not provided" in {
    val sourcePaths = "cucumber:/my/path/cucumber-reports"
    val nonCukePaths = Generator.getNonCukePaths(sourcePaths)
    assert(nonCukePaths.isEmpty)
  }

}