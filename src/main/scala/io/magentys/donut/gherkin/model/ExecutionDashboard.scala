package io.magentys.donut.gherkin.model

import org.joda.time.format.DateTimeFormat

case class ExecutionDashboard(totalFeatures: Int,
                              featureMetrics: Metrics,
                              allTestsMetrics: Metrics,
                              scenarioMetrics: Metrics,
                              unitTestMetrics: Metrics,
                              stepMetrics: Metrics,
                              totalTags: Int,
                              featureFailRate: Double,
                              featurePassRate: Double,
                              scenariosFailRate: Double,
                              scenariosPassRate: Double,
                              unitTestsFailRate: Double,
                              unitTestsPassRate: Double,
                              executionDuration: String,
                              executionDateTime: String)

object ExecutionDashboard {

  def apply(implicit features: List[Feature], executionData: ExecutionData): ExecutionDashboard = {

    val featuresMetrics = executionData.allFeatureMetrics
    val allTestsMetrics = executionData.allTestsMetrics
    val scenarioMetrics = executionData.allScenarioMetrics
    val unitTestMetrics = executionData.allUnitTestMetrics

    ExecutionDashboard(
      featuresMetrics.total,
      featuresMetrics,
      allTestsMetrics,
      scenarioMetrics,
      unitTestMetrics,
      executionData.allStepMetrics,
      executionData.allTagSize,
      failRate(featuresMetrics),
      passRate(featuresMetrics),
      failRate(scenarioMetrics),
      passRate(scenarioMetrics),
      failRate(unitTestMetrics),
      passRate(unitTestMetrics),
      Duration.calculateTotalDurationStr(features.map(f => f.duration.duration)),
      DateTimeFormat.forPattern("dd/MM/yyyy HH:mm").print(executionData.timestamp))
  }

  def passRate(metrics: Metrics) =
    rate(metrics.passed.toDouble, metrics.total.toDouble)

  def failRate(metrics: Metrics) =
    rate(metrics.failed.toDouble, metrics.total.toDouble)

  def rate(metric: Double, totalMetrics: Double) = {
    if (totalMetrics > 0)
      BigDecimal((metric / totalMetrics) * 100)
        .setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    else 0d
  }
}

