package io.magentys.donut.gherkin.model

import io.magentys.donut.gherkin.processors.ReportTag

/**
  * TODO: Revisit the naming for unit test variables
  * UnitTests or TotalUnitTests or unitTestMetrics.total refers to unit tests that are linked to a feature<br>
  * i.e. excluding orphaned unit tests(that are not linked to any feature). It may be slightly confusing and will <br>
  * be revisited later.
  */
case class ReportConsole(allFeatures: List[Feature],
                         allTags: List[ReportTag],
                         totalFeatures: Int,
                         numberOfPassedFeatures: Int,
                         numberOfFailedFeatures: Int,
                         totalScenarios: Int,
                         numberOfPassedScenarios: Int,
                         numberOfFailedScenarios: Int,
                         hasUnitTests:Boolean,
                         numberOfTotalUnitTests: Int,
                         numberOfPassedUnitTests: Int,
                         numberOfFailedUnitTests: Int,
                         numberOfTotalOrphanedUnitTests: Int, // Those that have feature name: Without feature
                         numberOfPassedOrphanedUnitTests: Int,
                         numberOfFailedOrphanedUnitTests: Int,
                         hasOrphanedUnitTests: Boolean,
                         totalSteps: Int,
                         numberOfPassedSteps: Int,
                         numberOfFailedSteps: Int,
                         numberOfSkippedSteps: Int,
                         numberOfPendingSteps: Int,
                         numberOfUndefinedSteps: Int,
                         duration: String,
                         buildFailed: Boolean)

object ReportConsole {
  def apply(report: Report): ReportConsole = {
    ReportConsole(
      report.featuresPage.features,
      report.tagPage.reportTag,
      report.dashboardPage.reportDashboard.featureMetrics.total,
      report.dashboardPage.reportDashboard.featureMetrics.passed,
      report.dashboardPage.reportDashboard.featureMetrics.failed,
      report.dashboardPage.reportDashboard.scenarioMetrics.total,
      report.dashboardPage.reportDashboard.scenarioMetrics.passed,
      report.dashboardPage.reportDashboard.scenarioMetrics.failed,
      report.dashboardPage.reportDashboard.unitTestMetrics.hasUnitTests,
      report.dashboardPage.reportDashboard.unitTestMetrics.total,
      report.dashboardPage.reportDashboard.unitTestMetrics.passed,
      report.dashboardPage.reportDashboard.unitTestMetrics.failed,
      report.dashboardPage.reportDashboard.unitTestMetrics.orphaned,
      report.dashboardPage.reportDashboard.unitTestMetrics.orphanedPassed,
      report.dashboardPage.reportDashboard.unitTestMetrics.orphanedFailed,
      report.dashboardPage.reportDashboard.unitTestMetrics.hasOrphanedUnitTests,
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