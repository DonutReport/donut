package io.magentys.donut.transformers.cucumber

import java.io.File

import io.magentys.donut.DonutTestData
import io.magentys.donut.gherkin.model
import io.magentys.donut.gherkin.model.Metrics
import io.magentys.donut.gherkin.processors.JSONProcessor
import org.json4s.JValue
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ListBuffer

class CucumberTransformerTest extends FlatSpec with Matchers {

  private val rootDir = List("src", "test", "resources", "samples-1").mkString("", File.separator, File.separator)
  private val values = JSONProcessor.loadFrom(new File(rootDir))
  private val features = values.right.flatMap { e => CucumberTransformer.transform(e, new ListBuffer[model.Feature], DonutTestData.statusConfiguration) }

  behavior of "CucumberAdaptor"

  it should "transform all files json values to list of Features" in {
    features.fold(
      e => fail(e),
      f => {
        f.size shouldBe 10
        f.head.name shouldBe "Google Journey Performance"
        f(1).name shouldBe "Google search"
        f(2).name shouldBe "Offset Actions"
        f(3).name shouldBe "Input Actions"
        f(4).name shouldBe "Mouse actions"
        f(5).name shouldBe "Performance"
        f(6).name shouldBe "Select"
        f(7).name shouldBe "Switch to window"
        f(8).name shouldBe "Tables"
        f(9).name shouldBe "Examples Tables"
      }
    )
  }

  it should "return empty list if there are no features" in {
    CucumberTransformer.transform(List.empty, new ListBuffer[model.Feature], DonutTestData.statusConfiguration) shouldEqual Right(List.empty)
  }

  it should "enhance scenarios with extra values" in {
    features match {
      case Left(e) => fail(e)
      case Right(f) =>
        val firstScenario = f.flatMap(_.scenarios).head
        firstScenario.status.status shouldEqual false
        firstScenario.status.statusStr shouldEqual "failed"
        firstScenario.featureName shouldEqual "Google Journey Performance"
        firstScenario.featureIndex shouldEqual "10000"
        firstScenario.duration.duration shouldEqual 7984105000L
        firstScenario.duration.durationStr shouldEqual "7 secs and 984 ms"
        firstScenario.screenshotsSize shouldEqual 1
        firstScenario.screenshotStyle shouldEqual ""
        //firstScenario.screenshotIDs shouldEqual(embeddings(0).data.hashCode.toString)
        firstScenario.background shouldEqual None
    }
  }

  it should "enhance steps with user friendly duration" in {
    features match {
      case Left(e) => fail(e)
      case Right(f) =>
        val enhancedSteps = f.flatMap(_.scenarios).flatMap(_.steps)
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
    val originalFeatures: List[Feature] = CucumberTransformer.loadCukeFeatures(values.right.get)
    val feature: model.Feature = CucumberTransformer.mapToDonutFeature(originalFeatures.head, "10000", DonutTestData.statusConfiguration)
    feature.isInstanceOf[model.Feature] shouldBe true
    feature.duration.duration shouldEqual 7984105000L
    feature.duration.durationStr shouldEqual "7 secs and 984 ms"
    feature.status.status shouldEqual false
    feature.status.statusStr shouldEqual "failed"
    feature.htmlFeatureTags shouldEqual List("google", "performance")
    feature.scenarioMetrics shouldEqual Metrics(1, 0, 1)
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
