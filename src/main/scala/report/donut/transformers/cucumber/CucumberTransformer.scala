package report.donut.transformers.cucumber

import report.donut.gherkin.model._
import report.donut.gherkin.model.{ScenarioMetrics, StatusConfiguration, Duration => DonutDuration, Embedding => DonutEmbedding, Feature => DonutFeature, Row => DonutRow, Scenario => DonutScenario, BeforeHook => DonutBeforeHook, Step => DonutStep, AfterHook => DonutAfterHook, Examples => DonutExamples}
import report.donut.gherkin.processors.{HTMLFeatureProcessor, ImageProcessor}
import report.donut.log.Log

import scala.collection.mutable.ListBuffer
import scala.util.{Either, Try}

object CucumberTransformer extends Log {

  def transform(features: List[Feature], conf: StatusConfiguration): Either[String, ListBuffer[DonutFeature]] = {
    Try(mapToDonutFeatures(features, new ListBuffer[DonutFeature], conf)).toEither(_.getMessage)
  }

  private[cucumber] def mapToDonutFeatures(features: List[Feature], donutFeatures: ListBuffer[DonutFeature], statusConfiguration: StatusConfiguration): ListBuffer[DonutFeature] = {
    var i = 0
    for (feature <- features) {
      if (isFeatureAlreadyAdded(feature.name, donutFeatures)) {
        val donutFeature = donutFeatures.find(df => df.name.equals(feature.name)).get
        val index = donutFeatures.indexOf(donutFeature)
        donutFeatures(index) = addScenariosToFeature(feature, donutFeature, statusConfiguration)
      } else {
        val index = if (donutFeatures.nonEmpty) donutFeatures.last.index.toInt + 1 else 10000 + i
        donutFeatures += mapToDonutFeature(feature, index.toString.trim, statusConfiguration)
        i += 1
      }
    }
    donutFeatures
  }

  private def isFeatureAlreadyAdded(name: String, donutFeatures: ListBuffer[DonutFeature]): Boolean = {
    donutFeatures.exists(df => df.name.equals(name))
  }

  private[cucumber] def addScenariosToFeature(feature: Feature, donutFeature: DonutFeature, statusConfiguration: StatusConfiguration): DonutFeature = {

    val scenarios = mapToDonutScenarios(feature.elements, feature.name, donutFeature.index, statusConfiguration)
    val combinedScenarios = donutFeature.scenarios ++ scenarios
    val scenariosExcludeBackground = combinedScenarios.filterNot(e => e.keyword == "Background")

    donutFeature.copy(
      scenarios = combinedScenarios,
      status = donutFeatureStatus(combinedScenarios, statusConfiguration),
      duration = donutFeatureDuration(combinedScenarios),
      scenarioMetrics = ScenarioMetrics(scenariosExcludeBackground),
      htmlElements = HTMLFeatureProcessor(scenariosExcludeBackground, donutFeature.index)
    )
  }

  private[cucumber] def mapToDonutFeature(feature: Feature, featureIndex: String, statusConfiguration: StatusConfiguration): DonutFeature = {

    val scenarios: List[Scenario] = mapToDonutScenarios(feature.elements, feature.name, featureIndex, statusConfiguration)
    val scenariosExcludeBackground = scenarios.filterNot(e => e.keyword == "Background")
    val tags = donutTags(feature.tags)

    DonutFeature(
      feature.keyword,
      feature.name,
      feature.description.getOrElse(""),
      feature.uri,
      scenarios,
      tags,
      donutFeatureStatus(scenarios, statusConfiguration),
      donutFeatureDuration(scenarios),
      ScenarioMetrics(scenariosExcludeBackground), //TODO what if background fails?
      Metrics(0, 0, 0),
      tags,
      HTMLFeatureProcessor(scenariosExcludeBackground, featureIndex),
      "cucumber", featureIndex)
  }

  private[cucumber] def mapToDonutScenarios(elements: List[Element], featureName: String, featureIndex: String, statusConfiguration: StatusConfiguration): List[Scenario] = {
    if (elements.nonEmpty) {
      if (elements.head.keyword == "Background") {
        elements.grouped(2).toList.flatMap(backgroundAndScenario => {
          val background = mapToDonutScenario(backgroundAndScenario.head, None, featureName, featureIndex, statusConfiguration)
          val scenario = mapToDonutScenario(backgroundAndScenario.tail(0), Some(background), featureName, featureIndex, statusConfiguration)
          List(background, scenario)
        })
      } else {
        elements.map(e => mapToDonutScenario(e, None, featureName, featureIndex, statusConfiguration))
      }
    } else {
      log.error(s"No scenarios found at: $featureName")
      List.empty
    }
  }

  private[cucumber] def mapToDonutScenario(e: Element, backgroundElement: Option[Scenario], featureName: String, featureIndex: String, statusConfiguration: StatusConfiguration): Scenario = {
    val screenshots = donutScenarioScreenshots(e)
    DonutScenario(
      e.description,
      e.name,
      e.keyword,
      donutTags(e.tags),
      e.steps.map(s => mapToDonutStep(s, statusConfiguration)),
      featureName,
      featureIndex,
      donutScenarioStatus(e, statusConfiguration),
      donutScenarioDuration(e),
      backgroundElement,
      screenshots.screenshotsSize,
      screenshots.screenshotsIds,
      screenshots.screenshotsStyle,
      e.`type`,
      e.examples.map(mapToDonutExamples),
      e.before.map(h => mapToDonutBeforeHook(h, statusConfiguration)),
      e.after.map(h => mapToDonutAfterHook(h, statusConfiguration)))
  }

  private[cucumber] def mapToDonutBeforeHook(h: BeforeHook, statusConfiguration: StatusConfiguration) = {
    DonutBeforeHook(h.output,
      Status(statusConfiguration, h.result.status),
      DonutDuration(h.result.duration),
      h.result.error_message)
  }

  private[cucumber] def mapToDonutStep(s: Step, statusConfiguration: StatusConfiguration) = {
    DonutStep(
      s.name,
      s.keyword,
      s.rows.map(r => DonutRow(r.cells)),
      s.output,
      Status(statusConfiguration, s.result.status),
      DonutDuration(s.result.duration),
      0L, 0L,
      s.result.error_message)
  }

  private[cucumber] def mapToDonutAfterHook(h: AfterHook, statusConfiguration: StatusConfiguration) = {
    DonutAfterHook(h.output,
      Status(statusConfiguration, h.result.status),
      DonutDuration(h.result.duration),
      h.result.error_message)
  }

  private[cucumber] def mapToDonutExamples(e: Examples) = {
    DonutExamples(
      e.name,
      e.keyword,
      e.description,
      e.rows.map(r => DonutRow(r.cells))
      )
  }

  private[cucumber] def donutFeatureStatus(scenarios: List[Scenario], statusConfiguration: StatusConfiguration) = {
    val elementsStatuses = scenarios.map(s => s.status.statusStr)
    val featureStatus = Status.calculate(statusConfiguration, elementsStatuses)
    val statusStr = if (featureStatus) Status.PASSED else Status.FAILED
    Status(featureStatus, statusStr)
  }

  private[cucumber] def donutFeatureDuration(scenarios: List[Scenario]) = {
    val elementsDuration = scenarios.map(e => e.duration.duration)
    val duration = DonutDuration.calculateTotalDuration(elementsDuration)
    DonutDuration(duration)
  }

  private[cucumber] def donutScenarioScreenshots(e: Element) = {
    val elementScreenshots: List[Embedding] = e.steps.flatMap(s => if (s.embeddings == null) e.after.flatMap(t => if (t.embeddings==null) null else t.embeddings) else s.embeddings)
    val screenshotsSize = elementScreenshots.size
    val screenshotStyle = if (elementScreenshots.nonEmpty) "" else "display:none;"
    val screenshots = elementScreenshots.map(e => DonutEmbedding(e.mime_type, e.data, e.id))
    val screenshotIDs: String = ImageProcessor.getScreenshotIds(screenshots)

    Screenshots(screenshotIDs, screenshotsSize, screenshotStyle)
  }

  private[cucumber] def donutScenarioDuration(e: Element) = {
    val stepDuration = e.steps.map(s => s.result.duration)
    val totalDuration = Duration.calculateTotalDuration(stepDuration)
    DonutDuration(totalDuration)
  }

  private[cucumber] def donutScenarioStatus(e: Element, statusConfiguration: StatusConfiguration) = {
    val stepStatuses = e.steps.map(s => s.result.status)
    val statusCalc = Status.calculate(statusConfiguration, stepStatuses)
    Status(statusCalc, if (statusCalc) Status.PASSED else Status.FAILED)
  }

  private[cucumber] def donutTags(tags: List[Tag]): List[String] = tags.map(t => t.name.substring(1))
}




