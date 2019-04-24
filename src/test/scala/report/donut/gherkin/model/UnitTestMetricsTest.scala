package report.donut.gherkin.model

import report.donut.DonutTestData
import org.scalatest.{FlatSpec, Matchers}

class UnitTestMetricsTest extends FlatSpec with Matchers {

  val featureWithUnit: List[Feature] = DonutTestData.featuresWithOnlyUnits.right.get

  it should "calculate unit test metrics if no orphaned tests" in {
    UnitTestMetrics(featureWithUnit.flatMap(f => f.unitTests)) shouldBe Metrics(1, 1, 0, hasUnitTests = true)
  }

  it should "calculate unit test metrics if both linked and orphaned tests are present" in {
    val unitTests: List[Scenario] = featureWithUnit.flatMap(f => f.unitTests)
    val orphaned: Scenario = unitTests.head.copy(featureName = Feature.DummyFeatureName)
    val unitTestsWithOrphaned = orphaned :: unitTests

    UnitTestMetrics(unitTestsWithOrphaned) shouldBe Metrics(1, 1, 0, orphaned = 1, orphanedPassed = 1, hasUnitTests = true, hasOrphanedUnitTests = true)
  }

  it should "calculate unit test metrics if no unit tests" in {
    UnitTestMetrics(List.empty) shouldBe Metrics(0, 0, 0)
  }

  it should "calculate unit test metrics if only orphaned tests" in {
    val unitTests: List[Scenario] = featureWithUnit.flatMap(f => f.unitTests)
    val orphaned: Scenario = unitTests.head.copy(featureName = Feature.DummyFeatureName)
    val unitTestsWithOrphaned = orphaned :: unitTests

    UnitTestMetrics(unitTestsWithOrphaned.dropRight(1)) shouldBe Metrics(0, 0, 0, orphaned = 1, orphanedPassed = 1, hasOrphanedUnitTests = true)
  }
}
