package io.magentys.donut.gherkin.processors

import io.magentys.donut.gherkin.model._
import org.json4s.jackson.Serialization
import org.json4s.{Formats, NoTypeHints, jackson}

case class ReportTag(tag: String,
                     scenarios: List[Scenario],
                     scenariosMetrics: Metrics,
                     tagStatus: String,
                     htmlElements: String = "")

case class TagMetricsForChart(tag: String, scenariosMetrics: Metrics)

object TagProcessor {

  def apply(features: List[Feature]): (List[ReportTag], String) = {

    val allReportTags = createAllReportTags(features)

    (allReportTags, createChart(allReportTags))
  }

 private[processors] def createChart(reportTags: List[ReportTag]): String = {
    implicit def json4sJacksonFormats: Formats = jackson.Serialization.formats(NoTypeHints)
    Serialization.writePretty(reportTags.map(t => new TagMetricsForChart(t.tag, t.scenariosMetrics)))
  }

  private[processors] def createAllReportTags(features: List[Feature]): List[ReportTag] = {
    val scenarios: List[Scenario] = features.flatMap(f => addFeatureTagsToScenarios(f.scenariosExcludeBackground, f.tags))
    groupElementsByTag(scenarios)
      .map { case (tag, scenarioList) => new ReportTag(tag, scenarioList, ScenarioMetrics(scenarioList), tagStatus(scenarioList)) }.toList
      .zipWithIndex.map { case (t, i) => t.copy(htmlElements = HTMLTagsProcessor(t.scenarios, i.toString.trim)) }
      .sortWith((left, right) => left.scenariosMetrics.total > right.scenariosMetrics.total)
  }

  // tagName -> List[Elements], excluding background elements
  private[processors] def groupElementsByTag(scenarios: List[Scenario]): Map[String, List[Scenario]] =
    scenarios.flatMap(s => s.tags.map(tag => (tag, s))).groupBy(_._1).mapValues(value => value.map(_._2))

  // Adds the parent (feature) tag to all children (scenarios)
  private[processors] def addFeatureTagsToScenarios(scenarios: List[Scenario], featureTags: List[String]): List[Scenario] =
    scenarios.map(e => e.copy(tags = (e.tags ::: featureTags).distinct))

  // Returns `passed` or `failed`
  private[processors] def tagStatus(scenarios: List[Scenario]): String = {
    val statuses = scenarios.map(s => s.status.statusStr)
    if (statuses.contains(Status.FAILED)) Status.FAILED else Status.PASSED
  }
}


