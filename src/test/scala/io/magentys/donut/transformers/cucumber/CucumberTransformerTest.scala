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

  val sample4RootDir = List("src", "test", "resources", "samples-4").mkString("", File.separator, File.separator)
  val sample4Values = JSONProcessor.loadFrom(new File(sample4RootDir))
  val sample4Features = CucumberTransformer.transform(sample4Values, DonutTestData.statusConfiguration)

  behavior of "CucumberAdaptor"

  it should "transform all files json values to list of Features" in {
    features.size shouldBe 9
    features.head.name shouldBe "Google Journey Performance"
    features(1).name shouldBe "Google search"
    features(2).name shouldBe "Offset Actions"
    features(3).name shouldBe "Input Actions"
    features(4).name shouldBe "Mouse actions"
    features(5).name shouldBe "Performance"
    features(6).name shouldBe "Select"
    features(7).name shouldBe "Switch to window"
    features(8).name shouldBe "Tables"
  }

  it should "group donut features by feature name while transforming the list of cucumber features" in {
    sample4Features.size shouldBe 1
    sample4Features.head.name shouldBe "Add numbers"

    val scenarios = sample4Features.head.scenarios
    scenarios.size shouldBe 3
    scenarios.head.name shouldBe "Add two numbers: 1 and 2"
    scenarios(1).name shouldBe "Add four numbers: 1,2,5,10"
    scenarios(2).name shouldBe "Only 1 number is provided"
  }

  it should "return empty list if there are no features" in {
    CucumberTransformer.transform(List.empty, DonutTestData.statusConfiguration) shouldEqual List.empty
  }

  it should "enhance scenarios with extra values" in {
    val enhancedScenarios = features.flatMap(f => f.scenarios)
    enhancedScenarios.head.status.status shouldEqual false
    enhancedScenarios.head.status.statusStr shouldEqual "failed"
    enhancedScenarios.head.featureName shouldEqual "Google Journey Performance"
    enhancedScenarios.head.featureIndex shouldEqual "10000"
    enhancedScenarios.head.duration.duration shouldEqual 7984105000L
    enhancedScenarios.head.duration.durationStr shouldEqual "7 secs and 984 ms"
    enhancedScenarios.head.screenshotsSize shouldEqual 1
    enhancedScenarios.head.screenshotStyle shouldEqual ""
    //    enhancedScenarios(0).screenshotIDs shouldEqual(embeddings(0).data.hashCode.toString)
    enhancedScenarios.head.background shouldEqual None
  }

  it should "enhance steps with user friendly duration" in {
    val enhancedSteps = features.flatMap(f => f.scenarios).flatMap(e => e.steps)
    enhancedSteps.head.duration.durationStr shouldEqual "7 secs and 977 ms"
    enhancedSteps(1).duration.durationStr shouldEqual "6 ms"
  }

  behavior of "CucumberAdaptor units"

  it should "mapToDonutFeatures" in {
    val originalFeatures: List[Feature] = CucumberTransformer.loadCukeFeatures(values)
    val generatedFeatures = CucumberTransformer.mapToDonutFeatures(originalFeatures, DonutTestData.statusConfiguration)
    generatedFeatures.size shouldEqual originalFeatures.size

    for {
      o <- originalFeatures
      g <- generatedFeatures
    } yield if (o.name == g.name) {
      o.elements.size shouldBe g.scenarios.size
      g.index.toInt shouldBe >=(10000)
    }
  }

  it should "mapToDonutFeatures if a feature is split across multiple json files" in {

    val originalFeatures: List[Feature] = CucumberTransformer.loadCukeFeatures(sample4Values)
    val generatedFeatures = CucumberTransformer.mapToDonutFeatures(originalFeatures, DonutTestData.statusConfiguration)
    val scenarios = generatedFeatures.head.scenarios

    originalFeatures.size shouldBe 3
    generatedFeatures.size shouldBe 1
    scenarios.size shouldBe 3
    generatedFeatures.head.index.toInt shouldBe 10000


    for (o <- originalFeatures) {
      o.name == generatedFeatures.head.name
    }
  }

  it should "mapToDonutFeature" in {
    val originalFeatures: List[Feature] = CucumberTransformer.loadCukeFeatures(values)
    val feature: model.Feature = CucumberTransformer.mapToDonutFeature(originalFeatures.head, "10000", DonutTestData.statusConfiguration)
    feature.isInstanceOf[model.Feature] shouldBe true
    feature.duration.duration shouldEqual 7984105000L
    feature.duration.durationStr shouldEqual "7 secs and 984 ms"
    feature.status.status shouldEqual false
    feature.status.statusStr shouldEqual "failed"
    feature.htmlFeatureTags shouldEqual List("google", "performance")
    feature.scenarioMetrics shouldEqual Metrics(1, 0, 1)
    feature.stepMetrics shouldEqual Metrics(0, 0, 0, 0, 0, 0, 0)
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
