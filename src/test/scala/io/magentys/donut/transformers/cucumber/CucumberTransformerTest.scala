package io.magentys.donut.transformers.cucumber

import java.io.File

import io.magentys.donut.DonutTestData
import io.magentys.donut.gherkin.model
import io.magentys.donut.gherkin.model.Metrics
import io.magentys.donut.gherkin.processors.JSONProcessor
import org.scalatest.{FlatSpec, Matchers}

class CucumberTransformerTest extends FlatSpec with Matchers {

  val rootDir = List("src", "test", "resources", "samples-1").mkString("", File.separator, File.separator)
  val values = JSONProcessor.loadFrom(new File(rootDir))
  val features = CucumberTransformer.transform(values, DonutTestData.statusConfiguration)

  behavior of "CucumberAdaptor"

  it should "transoform all files json values to list of Features" in {
      features.size shouldBe 9
      features(0).name shouldBe "Google Journey Performance"
      features(1).name shouldBe "Google search"
      features(2).name shouldBe "Offset Actions"
      features(3).name shouldBe "Input Actions"
      features(4).name shouldBe "Mouse actions"
      features(5).name shouldBe "Performance"
      features(6).name shouldBe "Select"
      features(7).name shouldBe "Switch to window"
      features(8).name shouldBe "Tables"
    }

  it should "return empty list if there are no features" in {
    CucumberTransformer.transform(List.empty, DonutTestData.statusConfiguration) shouldEqual List.empty
  }

  it should "enhance scenarios with extra values" in {
    val enhancedScenarios = features.flatMap(f => f.scenarios)
    enhancedScenarios(0).status.status shouldEqual(false)
    enhancedScenarios(0).status.statusStr shouldEqual("failed")
    enhancedScenarios(0).featureName shouldEqual("Google Journey Performance")
    enhancedScenarios(0).featureIndex shouldEqual("10000")
    enhancedScenarios(0).duration.duration shouldEqual(7984105000L)
    enhancedScenarios(0).duration.durationStr shouldEqual("7 secs and 984 ms")
    enhancedScenarios(0).screenshotsSize shouldEqual(1)
    enhancedScenarios(0).screenshotStyle shouldEqual("")
//    enhancedScenarios(0).screenshotIDs shouldEqual(embeddings(0).data.hashCode.toString)
    enhancedScenarios(0).background shouldEqual(None)
  }

  it should "enhance steps with user friendly duration" in {
    val enhancedSteps = features.flatMap(f => f.scenarios).flatMap(e => e.steps)
    enhancedSteps(0).duration.durationStr shouldEqual("7 secs and 977 ms")
    enhancedSteps(1).duration.durationStr shouldEqual("6 ms")
  }

  behavior of "CucumberAdaptor units"

  it should "mapToDonutFeatures" in {
    val originalFeatures: List[Feature] = CucumberTransformer.loadCukeFeatures(values)
    val generatedFeatures = CucumberTransformer.mapToDonutFeatures(originalFeatures, DonutTestData.statusConfiguration)
    generatedFeatures.size shouldEqual originalFeatures.size

    for {
      o <- originalFeatures
      g <- generatedFeatures
    } yield if(o.name == g.name) {
        o.elements.size shouldBe g.scenarios.size
        g.index.toInt shouldBe >= (10000)
    }
  }

  it should "mapToDonutFeature" in {
    val originalFeatures: List[Feature] = CucumberTransformer.loadCukeFeatures(values)
    val feature: model.Feature = CucumberTransformer.mapToDonutFeature(originalFeatures(0), "10000", DonutTestData.statusConfiguration)
    feature.isInstanceOf[model.Feature] shouldBe true
    feature.duration.duration shouldEqual (7984105000L)
    feature.duration.durationStr shouldEqual ("7 secs and 984 ms")
    feature.status.status shouldEqual (false)
    feature.status.statusStr shouldEqual ("failed")
    feature.htmlFeatureTags shouldEqual (List("google", "performance"))
    feature.scenarioMetrics shouldEqual (Metrics(1, 0, 1))
    feature.stepMetrics shouldEqual (Metrics(0, 0, 0, 0, 0, 0, 0))
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
