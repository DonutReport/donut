package io.magentys.donut.gherkin

import java.io.File

import io.magentys.donut.DonutTestData
import org.scalatest.{FlatSpec, Matchers}

class GeneratorTest extends FlatSpec with Matchers {

  behavior of "Generator"

  it should "should return error if it tries to parse an invalid file" in {
    Generator.createReport("cucumber:src/test/resources/samples-weirdos", projectName = "", projectVersion = "") shouldBe Left("Json could not be parsed for src/test/resources/samples-weirdos/invalid_format.json,")
  }

  it should "throw an exception if cucumber/specflow source directory is not provided" in {
    val sourcePaths = ""
    val exception = intercept[DonutException] {
      Generator.createReport(sourcePaths, projectName = "", projectVersion = "")
    }
    assert(exception.mgs === "Unable to extract the path to cucumber/specflow reports. Please use this format:- cucumber:/my/path/cucumber-reports,/my/path/nunit-reports")
  }

  it should "throw an exception if cucumber/specflow source path is not specified with correct identier 'cucumber:' or 'specflow'" in {
    val sourcePaths = "src/test/resources/samples-1"
    val exception = intercept[DonutException] {
      Generator.createReport(sourcePaths, projectName = "", projectVersion = "")
    }
    assert(exception.mgs === "Unable to extract the path to cucumber/specflow reports. Please use this format:- cucumber:/my/path/cucumber-reports,/my/path/nunit-reports")
  }

  it should "return an error message if cucumber/specflow source directory does not exist" in {
    Generator.createReport("cucumber:src/test/resources/non-existing-dir", projectName = "", projectVersion = "") shouldBe Left("Source directory doesn't exist")
  }

  it should "return an error message if json files not found" in {
    Generator.createReport("cucumber:src/test/resources/samples-empty", projectName = "", projectVersion = "") shouldBe Left("No files found of correct format")
  }

  it should "return report if valid json files are found" in {
    Generator.createReport("cucumber:src/test/resources/samples-1", projectName = "", projectVersion = "") match {
      case Left(e) => fail(e)
      case Right(r) =>
        r should not be null
    }
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
    assert(exception.mgs === "Please provide the source directory path.")
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

  it should "get non cuke path if it doesn't have 'cucumber:'" in {
    val sourcePaths = "cucumber:/my/path/cucumber-reports,/cucumber-and-unit/path/nunit-reports"
    val nonCukePaths = Generator.getNonCukePaths(sourcePaths)
    nonCukePaths.size shouldBe 1
    nonCukePaths.head shouldBe "/cucumber-and-unit/path/nunit-reports"
  }

  it should "get non cuke path if it doesn't have 'specflow:'" in {
    val sourcePaths = "cucumber:/my/path/cucumber-reports,/specflow-and-unit/path/nunit-reports"
    val nonCukePaths = Generator.getNonCukePaths(sourcePaths)
    nonCukePaths.size shouldBe 1
    nonCukePaths.head shouldBe "/specflow-and-unit/path/nunit-reports"
  }

  it should "return empty list if the non cuke source paths are not provided" in {
    val sourcePaths = "cucumber:/my/path/cucumber-reports"
    val nonCukePaths = Generator.getNonCukePaths(sourcePaths)
    assert(nonCukePaths.isEmpty)
  }

  it should "loadDonutFeatures even if 1 non cuke path is provided" in {
    Generator.loadDonutFeatures(new File("src/test/resources/cuke-and-unit/cuke"),List{"src/test/resources/cuke-and-unit/unit"},DonutTestData.statusConfiguration) match {
      case Left(e) => fail(e)
      case Right(f) =>
        f.size shouldBe 1
        f.head.name shouldBe "Add numbers"

        val scenarios = f.head.scenarios
        scenarios.head.name shouldBe "Add two numbers: 1 and 2"
        scenarios(1).name shouldBe "Only 1 number is provided"

        val unitTest =scenarios(2)
        unitTest.name shouldBe "Add four numbers: 1,2,5,10"
        unitTest.keyword shouldBe "Unit Test"
    }



  }


}