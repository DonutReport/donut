package io.magentys.donut.gherkin.model

case class Metrics(total: Int,
                   passed: Int,
                   failed: Int,
                   skipped: Int = 0,
                   pending: Int = 0,
                   undefined: Int = 0,
                   missing: Int = 0,
                   orphaned: Int = 0,
                   orphanedPassed: Int = 0,
                   orphanedFailed: Int = 0,
                   hasOrphanedUnitTests: Boolean = false,
                   hasUnitTests: Boolean = false)

object FeatureMetrics {
  def apply(features: List[Feature]): Metrics = {
    val failedFeatures = features.filterNot(a => a.status.status)
    Metrics(features.size, features.size - failedFeatures.size, failedFeatures.size)
  }
}

object ScenarioMetrics {
  def apply(scenarios: List[Scenario]): Metrics = {
    val passed = scenarios.count(s => s.status.status)
    val failed = scenarios.size - passed
    Metrics(scenarios.size, passed, failed)
  }
}

object UnitTestMetrics {
  def apply(scenarios: List[Scenario]): Metrics = {

    val orphanedUnitTests = scenarios.filter(s => s.featureName == Feature.DummyFeatureName)
    val orphanedUnitTestsCount = orphanedUnitTests.size
    val orphanedPassed = orphanedUnitTests.count(s => s.status.status)
    val orphanedFailed = orphanedUnitTests.size - orphanedPassed

    val linkedUnitTests = scenarios diff orphanedUnitTests
    val linkedUnitTestsCount = linkedUnitTests.size
    val passed = linkedUnitTests.count(s => s.status.status)
    val failed = linkedUnitTests.size - passed

    Metrics(linkedUnitTestsCount, passed, failed, 0, 0, 0, 0, orphanedUnitTestsCount, orphanedPassed, orphanedFailed, orphanedUnitTestsCount > 0, linkedUnitTestsCount > 0)
  }
}

object StepMetrics {
  def apply(steps: List[Step]): Metrics = {
    val passed = steps.count(s => s.status.statusStr == "passed")
    val failed = steps.count(s => s.status.statusStr == "failed")
    val skipped = steps.count(s => s.status.statusStr == "skipped")
    val pending = steps.count(s => s.status.statusStr == "pending")
    val undefined = steps.count(s => s.status.statusStr == "undefined")
    val missing = steps.count(s => s.status.statusStr == "missing")
    Metrics(steps.size, passed, failed, skipped, pending, undefined, missing)
  }
}