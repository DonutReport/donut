package io.magentys.donut.gherkin.model

import io.magentys.donut.gherkin.processors._
import org.joda.time.DateTime

case class Report(projectMetadata: ProjectMetadata,
                  dashboardPage: DashboardPage,
                  featuresPage: FeaturesPage,
                  tagPage: TagPage,
                  failuresPage: FailuresPage,
                  allImages: String)

object Report {
  def apply(features: List[Feature], timestamp: DateTime = DateTime.now, projectMetadata: ProjectMetadata) = {

    val executionData = ExecutionData(features, timestamp)
    new Report(projectMetadata,
      DashboardPage(features, executionData),
      FeaturesPage(features, executionData.allFeatureMetrics),
      TagPage(features),
      FailuresPage(executionData.allFailures),
      ImageProcessor.allImages)
  }
}

/**
  * Project metadata
  */
case class ProjectMetadata(projectName: String,
                           projectVersion: String,
                           customAttributes: Map[String, String] = Map())

/**
  * Features Page
  */
case class FeaturesPage(features: List[Feature],
                        featuresMetrics: Metrics,
                        totalDuration: String)

object FeaturesPage {
  def apply(features: List[Feature], allFeatureMetrics: Metrics) = {
    new FeaturesPage(features, allFeatureMetrics, Duration.calculateTotalDurationStr(features.map(f => f.duration.duration)))
  }
}

/**
  * Tags Page
  */
case class TagPage(reportTag: List[ReportTag],
                   totalTags: Int,
                   totalTagsPassed: Int,
                   totalTagsFailed: Int,
                   chart: String)

object TagPage {
  def apply(features: List[Feature]) = {
    val tags = TagProcessor(features)
    new TagPage(tags._1, 0, 0, 0, tags._2)
  }
}

/**
  * DashboardPage
  */
case class DashboardPage(reportDashboard: ExecutionDashboard)

object DashboardPage {
  def apply(features: List[Feature], executionData: ExecutionData) = {
    new DashboardPage(ExecutionDashboard(features, executionData))
  }
}

/**
  * Failures page
  */
case class FailuresPage(scenarios: List[Scenario],
                        totalFailures: Int,
                        htmlElements: String)

object FailuresPage {
  def apply(failedScenarios: List[Scenario]) = {
    new FailuresPage(failedScenarios, failedScenarios.size, HTMLFailuresProcessor(failedScenarios))
  }
}
