package report.donut.transformers.cucumber

import java.io.File

import org.json4s.DefaultFormats
import org.scalatest.{FlatSpec, Matchers}
import report.donut.DonutTestData
import report.donut.gherkin.model.{Metrics, Feature => DonutFeature}
import report.donut.gherkin.processors.JSONProcessor

import scala.collection.mutable.ListBuffer

class CucumberTransformerTest extends FlatSpec with Matchers {

  implicit val formats = DefaultFormats

  behavior of "CucumberTransformer"

  it should "transform all files json values to list of Features" in {
    val expectedFeatureNames = List("Google Journey Performance",
      "Examples Tables",
      "Google search",
      "Offset Actions",
      "Input Actions",
      "Mouse actions",
      "Performance",
      "Select",
      "Switch to window",
      "Tables"
    )
    val rootDir = List("src", "test", "resources", "mix-cucumber-1-and-4").mkString("", File.separator, File.separator)
    val features = JSONProcessor.loadFrom(new File(rootDir)).right.get.flatMap(f => f.extract[List[Feature]])
    val donutFeatures = CucumberTransformer.transform(features, DonutTestData.statusConfiguration)
    donutFeatures.fold(
      e => fail(e),
      f => {
        f.size shouldBe 10
        f.map(feature => feature.name).toList.sorted shouldBe expectedFeatureNames.sorted
      }
    )
  }

  it should "return empty list if there are no features" in {
    CucumberTransformer.transform(List.empty, DonutTestData.statusConfiguration) shouldEqual Right(List.empty)
  }

  it should "enhance scenarios with extra values" in {
    val rootDir = List("src", "test", "resources", "mix-cucumber-1-and-4").mkString("", File.separator, File.separator)
    val features = JSONProcessor.loadFrom(new File(rootDir)).right.get.flatMap(f => f.extract[List[Feature]])
    val donutFeatures = CucumberTransformer.transform(features, DonutTestData.statusConfiguration)
    donutFeatures match {
      case Left(e) => fail(e)
      case Right(f) =>
        val performanceScenario = f.flatMap(_.scenarios).filter(s => s.featureName == "Google Journey Performance").head
        performanceScenario.status.status shouldEqual false
        performanceScenario.status.statusStr shouldEqual "failed"
        performanceScenario.featureName shouldEqual "Google Journey Performance"
        performanceScenario.duration.duration shouldEqual 7984105000L
        performanceScenario.duration.durationStr shouldEqual "7 secs and 984 ms"
        performanceScenario.screenshotsSize shouldEqual 1
        performanceScenario.screenshotStyle shouldEqual ""
        performanceScenario.background shouldEqual None
    }
  }

  it should "enhance steps with user friendly duration" in {
    val rootDir = List("src", "test", "resources", "mix-cucumber-1-and-4").mkString("", File.separator, File.separator)
    val features = JSONProcessor.loadFrom(new File(rootDir)).right.get.flatMap(f => f.extract[List[Feature]])
    val donutFeatures = CucumberTransformer.transform(features, DonutTestData.statusConfiguration)
    donutFeatures match {
      case Left(e) => fail(e)
      case Right(f) =>
        val enhancedSteps = f.flatMap(_.scenarios).filter(s => s.featureName == "Google Journey Performance").flatMap(_.steps)
        enhancedSteps.head.duration.durationStr shouldEqual "7 secs and 977 ms"
        enhancedSteps(1).duration.durationStr shouldEqual "6 ms"
    }
  }

  behavior of "CucumberTransformer units"

  // TODO: Add more tests for samples that aren't supposed to be loaded

  it should "mapToDonutFeatures" in {
    val rootDir = List("src", "test", "resources", "all-pass").mkString("", File.separator, File.separator)
    val features = JSONProcessor.loadFrom(new File(rootDir)).right.get.flatMap(f => f.extract[List[Feature]])
    val generatedFeatures = CucumberTransformer.mapToDonutFeatures(features, new ListBuffer[DonutFeature], DonutTestData.statusConfiguration)
    generatedFeatures.size shouldEqual features.size

    for {
      o <- features
      g <- generatedFeatures
    } yield if (o.name == g.name) {
      o.elements.size shouldBe g.scenarios.size
      g.index.toInt shouldBe >=(10000)
    }
  }

  it should "mapToDonutFeature" in {
    val rootDir = List("src", "test", "resources", "samples-2").mkString("", File.separator, File.separator)
    val values = JSONProcessor.loadFrom(new File(rootDir))
    val features = JSONProcessor.loadFrom(new File(rootDir)).right.get.flatMap(f => f.extract[List[Feature]])
    val feature: DonutFeature = CucumberTransformer.mapToDonutFeature(features.head, "10000", DonutTestData.statusConfiguration)
    feature.isInstanceOf[DonutFeature] shouldBe true
    feature.duration.duration shouldEqual 7984105000L
    feature.duration.durationStr shouldEqual "7 secs and 984 ms"
    feature.status.status shouldEqual false
    feature.status.statusStr shouldEqual "failed"
    feature.htmlFeatureTags.sorted shouldBe List("google", "performance").sorted
    feature.scenarioMetrics shouldEqual Metrics(1, 0, 1, hasScenarios = true)
    feature.stepMetrics shouldEqual Metrics(0, 0, 0)
    feature.index shouldEqual "10000"
  }


  //  it should "mapToDonutScenario" in {
  //
  //  }
  //
  //  it should "mapToDonutStep" in {
  //
  //  }
  //
  //  it should "donutFeatureStatus" in {
  //
  //  }
  //
  //  it should "donutScenarioScreenshots" in {
  //
  //  }
  //
  //  it should "donutScenarioDuration" in {
  //
  //  }
  //
  //  it should "donutScenarioStatus" in {
  //
  //  }
}
