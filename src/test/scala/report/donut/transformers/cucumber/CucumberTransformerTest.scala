package report.donut.transformers.cucumber

import java.io.File

import report.donut.DonutTestData
import report.donut.gherkin.model
import report.donut.gherkin.model.Metrics
import report.donut.gherkin.processors.JSONProcessor
import org.json4s.JValue
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ListBuffer

class CucumberTransformerTest extends FlatSpec with Matchers {

  behavior of "CucumberAdaptor"

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
    val rootDir = List("src", "test", "resources", "mix-gherkin-2-and-5").mkString("", File.separator, File.separator)
    val values = JSONProcessor.loadFrom(new File(rootDir))
    val features = values.right.flatMap { e => CucumberTransformer.transform(e, new ListBuffer[model.Feature], DonutTestData.statusConfiguration) }
    features.fold(
      e => fail(e),
      f => {
        f.size shouldBe 10
        f.map(feature => feature.name).toList.sorted shouldBe expectedFeatureNames.sorted
      }
    )
  }

  it should "return empty list if there are no features" in {
    CucumberTransformer.transform(List.empty, new ListBuffer[model.Feature], DonutTestData.statusConfiguration) shouldEqual Right(List.empty)
  }

  it should "enhance scenarios with extra values" in {
    val rootDir = List("src", "test", "resources", "mix-gherkin-2-and-5").mkString("", File.separator, File.separator)
    val values = JSONProcessor.loadFrom(new File(rootDir))
    val features = values.right.flatMap { e => CucumberTransformer.transform(e, new ListBuffer[model.Feature], DonutTestData.statusConfiguration) }
    features match {
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
    val rootDir = List("src", "test", "resources", "mix-gherkin-2-and-5").mkString("", File.separator, File.separator)
    val values = JSONProcessor.loadFrom(new File(rootDir))
    val features = values.right.flatMap { e => CucumberTransformer.transform(e, new ListBuffer[model.Feature], DonutTestData.statusConfiguration) }
    features match {
      case Left(e) => fail(e)
      case Right(f) =>
        val enhancedSteps = f.flatMap(_.scenarios).filter(s => s.featureName == "Google Journey Performance").flatMap(_.steps)
        enhancedSteps.head.duration.durationStr shouldEqual "7 secs and 977 ms"
        enhancedSteps(1).duration.durationStr shouldEqual "6 ms"
    }
  }

  behavior of "CucumberAdaptor units"

  it should "loadCukeFeatures" in {
    val rootDir = List("src", "test", "resources", "samples-7").mkString("", File.separator, File.separator)
    val values = JSONProcessor.loadFrom(new File(rootDir))
    val originalFeatures: List[Feature] = CucumberTransformer.loadCukeFeatures(values.right.get)

    originalFeatures.size shouldEqual 1
  }

  // TODO: Add more tests for samples that aren't supposed to be loaded

  it should "mapToDonutFeatures" in {
    val rootDir = List("src", "test", "resources", "all-pass").mkString("", File.separator, File.separator)
    val values = JSONProcessor.loadFrom(new File(rootDir))
    val originalFeatures: List[Feature] = CucumberTransformer.loadCukeFeatures(values.right.get)
    val generatedFeatures = CucumberTransformer.mapToDonutFeatures(originalFeatures, new ListBuffer[model.Feature], DonutTestData.statusConfiguration)
    generatedFeatures.size shouldEqual originalFeatures.size

    for {
      o <- originalFeatures
      g <- generatedFeatures
    } yield if (o.name == g.name) {
      o.elements.size shouldBe g.scenarios.size
      g.index.toInt shouldBe >=(10000)
    }
  }

  it should "mapToDonutFeature" in {
    val rootDir = List("src", "test", "resources", "samples-2").mkString("", File.separator, File.separator)
    val values = JSONProcessor.loadFrom(new File(rootDir))
    val originalFeatures: List[Feature] = CucumberTransformer.loadCukeFeatures(values.right.get)
    val feature: model.Feature = CucumberTransformer.mapToDonutFeature(originalFeatures.head, "10000", DonutTestData.statusConfiguration)
    feature.isInstanceOf[model.Feature] shouldBe true
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
