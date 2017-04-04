package io.magentys.donut.gherkin.model

import io.magentys.donut.DonutTestData
import org.scalatest.{FlatSpec, Matchers}

class MetricsTest extends FlatSpec with Matchers {

  behavior of "Metrics Calculator"

  val features = DonutTestData.features_sample_3.right.get

  it should "calculate features metrics" in {
    FeatureMetrics(features) shouldBe Metrics(2, 1, 1, 0, 0, 0, 0)
  }

  it should "calculate features metrics if no Features" in {
    FeatureMetrics(List.empty) shouldBe Metrics(0, 0, 0, 0, 0, 0, 0)
  }

  it should "calculate scenario metrics" in {
    ScenarioMetrics(features.flatMap(f => f.scenariosExcludeBackgroundAndUnitTests)) shouldBe Metrics(2, 1, 1, 0, 0, 0, 0)
  }

  it should "calculate scenario metrics if no scenarios" in {
    ScenarioMetrics(List.empty) shouldBe Metrics(0, 0, 0, 0, 0, 0, 0)
  }

  it should "calculate step metrics" in {
    val steps = features.flatMap(f => f.scenariosExcludeBackgroundAndUnitTests).flatMap(s => s.steps)
    StepMetrics(steps) shouldBe Metrics(4, 3, 1, 0, 0, 0, 0)
  }

  it should "calculate step metrics if no steps" in {
    val steps = List.empty
    StepMetrics(steps) shouldBe Metrics(0, 0, 0, 0, 0, 0, 0)
  }
}
