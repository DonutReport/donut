package io.magentys.donut.gherkin.model

import io.magentys.donut.gherkin.processors.ReportTag

case class ReportConsole(allFeatures: List[Feature],
                        allTags: List[ReportTag],
                        totalFeatures: Int,
                        numberOfPassedFeatures: Int,
                        numberOfFailedFeatures: Int,
                        totalScenarios: Int,
                        numberOfPassedScenarios: Int,
                        numberOfFailedScenarios: Int,
                        totalSteps: Int,
                        numberOfPassedSteps: Int,
                        numberOfFailedSteps: Int,
                        numberOfSkippedSteps: Int,
                        numberOfPendingSteps: Int,
                        numberOfUndefinedSteps: Int,
                        duration: String,
                        buildFailed: Boolean)

object ReportConsole {
  def apply(report:Report): ReportConsole = {
    ReportConsole(
      report.featuresPage.features,
      report.tagPage.reportTag,
      report.dashboardPage.reportDashboard.featureMetrics.total,
      report.dashboardPage.reportDashboard.featureMetrics.passed,
      report.dashboardPage.reportDashboard.featureMetrics.failed,
      report.dashboardPage.reportDashboard.scenarioMetrics.total,
      report.dashboardPage.reportDashboard.scenarioMetrics.passed,
      report.dashboardPage.reportDashboard.scenarioMetrics.failed,
      report.dashboardPage.reportDashboard.stepMetrics.total,
      report.dashboardPage.reportDashboard.stepMetrics.passed,
      report.dashboardPage.reportDashboard.stepMetrics.failed,
      report.dashboardPage.reportDashboard.stepMetrics.skipped,
      report.dashboardPage.reportDashboard.stepMetrics.pending,
      report.dashboardPage.reportDashboard.stepMetrics.undefined,
      report.dashboardPage.reportDashboard.executionDuration,
      report.dashboardPage.reportDashboard.scenarioMetrics.failed > 0)
  }
}