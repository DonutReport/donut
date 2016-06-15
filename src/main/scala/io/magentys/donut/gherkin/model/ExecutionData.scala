package io.magentys.donut.gherkin.model

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
                         allScenarioMetrics: Metrics, //Without background scenarios counted
                         allStepMetrics: Metrics)

object ExecutionData {

  def apply(features: List[Feature], timestamp: DateTime) = {
    new ExecutionData(timestamp, allScenarios(features), allFailures(features), features.size, allTags(features).size,
      FeatureMetrics(features), ScenarioMetrics(allScenarios(features)), StepMetrics(allSteps(features)))
  }

  def allScenarios(features: List[Feature]): List[Scenario] =
    features.flatMap(f => f.scenariosExcludeBackground)

  def allSteps(features: List[Feature]): List[Step] = {
    features.flatMap(f => f.scenarios.map(e => e.steps)).flatten
  }

  def allTags(features: List[Feature]): Seq[String] = {
    val allFeatureTags = features.flatMap(f => f.tags)
    val allScenarioTags = features.flatMap(f => f.scenarios.flatMap(e => e.tags))
    (allFeatureTags ++ allScenarioTags).distinct
  }

  def allFailures(features: List[Feature]): List[Scenario] =
    allScenarios(features).filterNot(a => a.status.status) //Failures are without backgrounds here
}


