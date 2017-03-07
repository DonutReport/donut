package io.magentys.donut.transformers.cucumber

import io.magentys.donut.gherkin.model
import io.magentys.donut.gherkin.model._
import io.magentys.donut.gherkin.processors.{HTMLFeatureProcessor, ImageProcessor}
import io.magentys.donut.log.Log
import org.json4s._

import scala.collection.mutable.ListBuffer

object CucumberTransformer extends Log {

  def transform(json: List[JValue], conf: StatusConfiguration): List[model.Feature] = {
    mapToDonutFeatures(loadCukeFeatures(json), conf)
  }

  private[cucumber] def loadCukeFeatures(json: List[JValue]) = {
    implicit val formats = DefaultFormats
    json.flatMap(f => f.extractOpt[List[Feature]]).flatten
  }

  private[cucumber] def mapToDonutFeatures(features: List[Feature], statusConfiguration: StatusConfiguration): List[model.Feature] = {
    var donutFeatures = new ListBuffer[model.Feature]
    var i = 0
    for (feature <- features) {

      if (isFeatureAlreadyAdded(feature.name, donutFeatures)) {
        val donutFeature = donutFeatures.find(df => df.name.equals(feature.name)).get
        val index = donutFeatures.indexOf(donutFeature)
        donutFeatures(index) = addScenariosToFeature(feature, donutFeature, statusConfiguration)

      } else {
        val index = 10000 + i
        donutFeatures += mapToDonutFeature(feature, index.toString.trim, statusConfiguration)
        i += 1
      }
    }
    donutFeatures.toList
  }

  private def isFeatureAlreadyAdded(name: String, donutFeatures: ListBuffer[model.Feature]): Boolean = {
    donutFeatures.exists(df => df.name.equals(name))
  }

  private[cucumber] def addScenariosToFeature(feature: Feature, donutFeature: model.Feature, statusConfiguration: StatusConfiguration): model.Feature = {

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

  private[cucumber] def mapToDonutFeature(feature: Feature, featureIndex: String, statusConfiguration: StatusConfiguration) = {

    val scenarios: List[Scenario] = mapToDonutScenarios(feature.elements, feature.name, featureIndex, statusConfiguration)
    val scenariosExcludeBackground = scenarios.filterNot(e => e.keyword == "Background")
    val tags = donutTags(feature.tags)

    model.Feature(
      feature.keyword,
      feature.name,
      feature.description,
      feature.uri,
      scenarios,
      tags,
      donutFeatureStatus(scenarios, statusConfiguration),
      donutFeatureDuration(scenarios),
      ScenarioMetrics(scenariosExcludeBackground), //TODO what if background fails?
      Metrics(0, 0, 0, 0, 0),
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
    model.Scenario(
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
      screenshots.screenshotsStyle)
  }

  private[cucumber] def mapToDonutStep(s: Step, statusConfiguration: StatusConfiguration) = {
    model.Step(
      s.name,
      s.keyword,
      s.rows.map(r => model.Row(r.cells)),
      s.output,
      Status(statusConfiguration, s.result.status),
      Duration(s.result.duration),
      0L, 0L,
      s.result.error_message)
  }

  private[cucumber] def donutFeatureStatus(scenarios: List[Scenario], statusConfiguration: StatusConfiguration) = {
    val elementsStatuses = scenarios.map(s => s.status.statusStr)
    val featureStatus = Status.calculate(statusConfiguration, elementsStatuses)
    val statusStr = if (featureStatus) Status.PASSED else Status.FAILED
    Status(featureStatus, statusStr)
  }

  private[cucumber] def donutFeatureDuration(scenarios: List[Scenario]) = {
    val elementsDuration = scenarios.map(e => e.duration.duration)
    val duration = Duration.calculateTotalDuration(elementsDuration)
    Duration(duration)
  }

  private[cucumber] def donutScenarioScreenshots(e: Element) = {
    val elementScreenshots: List[Embedding] = e.steps.flatMap(s => s.embeddings)
    val screenshotsSize = elementScreenshots.size
    val screenshotStyle = if (elementScreenshots.nonEmpty) "" else "display:none;"
    val screenshots = elementScreenshots.map(e => model.Embedding(e.mime_type, e.data, e.id))
    val screenshotIDs: String = ImageProcessor.getScreenshotIds(screenshots)
    Screenshots(screenshotIDs, screenshotsSize, screenshotStyle)
  }

  private[cucumber] def donutScenarioDuration(e: Element) = {
    val stepDuration = e.steps.map(s => s.result.duration)
    val totalDuration = Duration.calculateTotalDuration(stepDuration)
    Duration(totalDuration)
  }

  private[cucumber] def donutScenarioStatus(e: Element, statusConfiguration: StatusConfiguration) = {
    val stepStatuses = e.steps.map(s => s.result.status)
    val statusCalc = Status.calculate(statusConfiguration, stepStatuses)
    Status(statusCalc, if (statusCalc) Status.PASSED else Status.FAILED)
  }

  private[cucumber] def donutTags(tags: List[Tag]): List[String] = tags.map(t => t.name.substring(1))

}




