package report.donut.gherkin.model

import org.joda.time.DateTime

/**
  * Metadata and Metrics for an execution
  */
case class ExecutionData(timestamp: DateTime,
                         allScenarios: List[Scenario],
                         allFailures: List[Scenario],
                         allFeaturesSize: Int,
                         allTagSize: Int,
                         allFeatureMetrics: Metrics,
                         allTestsMetrics: Metrics,
                         allScenarioMetrics: Metrics, //Without background scenarios counted
                         allUnitTestMetrics: Metrics,
                         allStepMetrics: Metrics)

object ExecutionData {

  def apply(features: List[Feature], timestamp: DateTime) = {
    new ExecutionData(timestamp, allScenarios(features), allFailures(features), features.size, allTags(features).size,
      FeatureMetrics(allFeatures(features)), AllTestsMetrics(allScenariosAndUnitTests(features)), ScenarioMetrics(allScenarios(features)), UnitTestMetrics(allUnitTests(features)), StepMetrics(scenarioSteps(features)))
  }

  def allFeatures(features: List[Feature]): List[Feature] =
    features.filterNot(f => f.name == Feature.DummyFeatureName)

  def allScenarios(features: List[Feature]): List[Scenario] =
    features.flatMap(f => f.scenariosExcludeBackgroundAndUnitTests)

  def allUnitTests(features: List[Feature]): List[Scenario] =
    features.flatMap(f => f.unitTests)

  def allScenariosAndUnitTests(features: List[Feature]): List[Scenario] =
    features.flatMap(f => f.scenariosExcludeBackground)

  def scenarioSteps(features: List[Feature]): List[Step] = {
    features.flatMap(f => (f.scenarios diff f.unitTests).map(e => e.steps)).flatten
  }

  // gherkin 2 - combine feature and scenario tags
  // gherkin 5 - scenarios inherit feature tags
  def allTags(features: List[Feature]): Seq[String] = {
    val allFeatureTags = features.flatMap(f => f.tags)
    val allScenarioTags = features.flatMap(f => f.scenarios.flatMap(e => e.tags))
    (allFeatureTags ++ allScenarioTags).distinct
  }

  def allFailures(features: List[Feature]): List[Scenario] =
    allScenariosAndUnitTests(features).filterNot(a => a.status.status) //Failures are without backgrounds here
}
