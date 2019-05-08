package report.donut

import java.io.File

import org.json4s.DefaultFormats
import org.scalatest.{FlatSpec, Matchers}
import report.donut.ResultLoader.CucumberResultLoader
import report.donut.gherkin.processors.JSONProcessor

class ResultLoaderTest extends FlatSpec with Matchers {

  implicit val formats = DefaultFormats

  behavior of "ResultLoader"

  it should "return a CucumberResultLoader if result source specifies a cucumber format" in {
    val sourcePath = List("cucumber:target", "cucumber-reports").mkString("", File.separator, "")
    val loader = ResultLoader(sourcePath)
    loader.isInstanceOf[CucumberResultLoader] shouldBe true
  }

  it should "return a CucumberResultLoader if result source does not specify a format" in {
    val sourcePath = List("target", "cucumber-reports").mkString("", File.separator, "")
    val loader = ResultLoader(sourcePath)
    loader.isInstanceOf[CucumberResultLoader] shouldBe true
  }

  it should "throw a DonutException if the result source format is not supported" in {
    val sourcePath = List("junit:target", "failsafe-reports").mkString("", File.separator, "")
    val exception = intercept[DonutException] {
      ResultLoader(sourcePath)
    }
    assert(exception.mgs === "Unsupported result format: junit")
  }

  it should "throw a DonutException if the result source format is provided without the path" in {
    val exception = intercept[DonutException] {
      ResultLoader("cucumber:")
    }
    assert(exception.mgs === "Please provide the source directory path.")
  }

  it should "return a CucumberResultLoader if result source specifies a format and an absolute Windows path" in {
    val sourcePath = "cucumber:C:\\Users\\JDoe\\project\\target\\adapted-reports"
    val loader = ResultLoader(sourcePath)
    loader.isInstanceOf[CucumberResultLoader] shouldBe true
  }

  it should "return a CucumberResultLoader if result source specifies a format and an absolute Unix path" in {
    val sourcePath = "cucumber:/home/jdoe/project/target/cucumber-reports"
    val loader = ResultLoader(sourcePath)
    loader.isInstanceOf[CucumberResultLoader] shouldBe true
  }

  it should "return feature list if valid cucumber JSON files are found" in {
    val sourcePath = List("cucumber:src", "test", "resources", "all-pass").mkString("", File.separator, "")
    val loader = ResultLoader(sourcePath)
    loader.load() match {
      case Left(e) => fail(e)
      case Right(r) =>
        r should not be null
        r.size shouldBe 10
    }
  }

  it should "return an error message for a non-existent source directory" in {
    val sourcePath = List("cucumber:src", "test", "resources", "non-existent").mkString("", File.separator, "")
    val loader = ResultLoader(sourcePath)
    loader.load() shouldBe Left(s"Source directory does not exist: ${sourcePath.replace("cucumber:", "")}")
  }

  it should "return an error message if JSON files are not found in the source directory" in {
    val sourcePath = List("cucumber:src", "test", "resources", "samples-empty").mkString("", File.separator, "")
    val loader = ResultLoader(sourcePath)
    loader.load() shouldBe Left("No files found of correct format")
  }

  it should "return an error if parsing an invalid JSON file" in {
    val sourcePath = List("cucumber:src", "test", "resources", "samples-weirdos").mkString("", File.separator, "")
    val jsonPath = List("src", "test", "resources", "samples-weirdos", "invalid_format.json").mkString("", File.separator, "")
    val loader = ResultLoader(sourcePath)
    loader.load() shouldBe Left("Json could not be parsed for " + jsonPath + ",")
  }

  behavior of "ResultLoader units"

  it should "loadCukeFeatures" in {
    val rootDir = List("src", "test", "resources", "samples-7").mkString("", File.separator, "")
    val features = JSONProcessor.loadFrom(new File(rootDir)).right.get
    val loader = new CucumberResultLoader(new File(rootDir))
    loader.loadCukeFeatures(features).size shouldEqual 1
  }
}
