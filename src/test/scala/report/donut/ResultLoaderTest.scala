package report.donut

import java.io.File

import org.json4s.DefaultFormats
import org.scalatest.{FlatSpec, Matchers}
import report.donut.ResultLoader.GherkinResultLoader
import report.donut.gherkin.model
import report.donut.gherkin.processors.JSONProcessor
import report.donut.transformers.cucumber.{CucumberTransformer, Feature}

class ResultLoaderTest extends FlatSpec with Matchers {

  implicit val formats = DefaultFormats

  behavior of "ResultLoader"

  it should "return a GherkinResultLoader if source path specifies a gherkin format" in {
    val sourcePath = List("gherkin:src", "test", "resources", "all-pass").mkString("", File.separator, "")
    val loader = ResultLoader(sourcePath)
    loader.isInstanceOf[GherkinResultLoader] shouldBe true
  }

  it should "return a GherkinResultLoader if source path does not specify a format" in {
    val sourcePath = List("gherkin:src", "test", "resources", "all-pass").mkString("", File.separator, "")
    val loader = ResultLoader(sourcePath)
    loader.isInstanceOf[GherkinResultLoader] shouldBe true
  }

  it should "throw a DonutException for a non-existent source directory" in {
    val sourcePath = List("gherkin:src", "test", "resources", "non-existent").mkString("", File.separator, "")
    val exception = intercept[DonutException] {
      ResultLoader(sourcePath)
    }
    assert(exception.mgs === s"Source directory does not exist: ${sourcePath.replace("gherkin:", "")}")
  }

  it should "throw a DonutException if the source format is not supported" in {
    val sourcePath = List("junit:src", "test", "resources", "all-pass").mkString("", File.separator, "")
    val exception = intercept[DonutException] {
      ResultLoader(sourcePath)
    }
    assert(exception.mgs === "Unsupported result format: junit")
  }

  it should "throw a DonutException if the source format is provided without the path" in {
    val exception = intercept[DonutException] {
      ResultLoader("gherkin:")
    }
    assert(exception.mgs === "Please provide the source directory path.")
  }

  it should "return feature list if valid gherkin JSON files are found" in {
    val sourcePath = List("gherkin:src", "test", "resources", "all-pass").mkString("", File.separator, "")
    val loader = ResultLoader(sourcePath)
    loader.load() match {
      case Left(e) => fail(e)
      case Right(r) =>
        r should not be null
        r.size shouldBe 10
    }
  }

  it should "return an error message if JSON files are not found in the source directory" in {
    val sourcePath = List("gherkin:src", "test", "resources", "samples-empty").mkString("", File.separator, "")
    val loader = ResultLoader(sourcePath)
    loader.load() shouldBe Left("No files found of correct format")
  }

  it should "return an error if parsing an invalid JSON file" in {
    val sourcePath = List("gherkin:src", "test", "resources", "samples-weirdos").mkString("", File.separator, "")
    val jsonPath = List("src", "test", "resources", "samples-weirdos", "invalid_format.json").mkString("", File.separator, "")
    val loader = ResultLoader(sourcePath)
    loader.load() shouldBe Left("Json could not be parsed for " + jsonPath + ",")
  }

  behavior of "ResultLoader units"

  it should "loadCukeFeatures" in {
    val rootDir = List("src", "test", "resources", "samples-7").mkString("", File.separator, "")
    val features = JSONProcessor.loadFrom(new File(rootDir)).right.get.flatMap(f => f.extract[List[Feature]])
    val donutFeatures: List[model.Feature] = CucumberTransformer.transform(features, DonutTestData.statusConfiguration).right.get.toList
    donutFeatures.size shouldEqual 1
  }
}
