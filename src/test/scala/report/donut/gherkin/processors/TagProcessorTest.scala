package report.donut.gherkin.processors

import report.donut.DonutTestData
import report.donut.gherkin.model._
import org.scalatest.{FlatSpec, Matchers}

class TagProcessorTest extends FlatSpec with Matchers {

  val features = DonutTestData.features_sample_2.right.get

  val scenarios = features.flatMap(f => f.scenarios)
  val featureTags = features.flatMap(f => f.tags)

  behavior of "TagProcessor"

  it should "add feature tags to scenarios, so that it can reorganise the scenarios" in {
    val result: List[Scenario] = TagProcessor.addFeatureTagsToScenarios(scenarios, featureTags)
    result.flatMap(s => s.tags) shouldBe List("complete", "google", "performance")
  }

  it should "rearrange all execution scenarios grouping by tag " in {
    val allExecutionScenariosWithFeatureTags = TagProcessor.addFeatureTagsToScenarios(scenarios, featureTags)
    val result = TagProcessor.groupElementsByTag(allExecutionScenariosWithFeatureTags)
    result.keys shouldBe Set("performance", "complete", "google")
    result.values.toList shouldBe List(allExecutionScenariosWithFeatureTags, allExecutionScenariosWithFeatureTags, allExecutionScenariosWithFeatureTags)
  }

  it should "create a list of ReportTags" in {
    val allExecutionScenariosWithFeatureTags = TagProcessor.addFeatureTagsToScenarios(scenarios, featureTags)
    val reportTags = TagProcessor.createAllReportTags(features)
    reportTags.length shouldBe 3
    reportTags.head.tag shouldBe "performance"
    reportTags.head.tagStatus shouldBe "failed"
    reportTags.head.scenarios shouldBe allExecutionScenariosWithFeatureTags
    reportTags.head.scenariosMetrics shouldBe Metrics(1, 0, 1, hasScenarios = true)

    reportTags(1).tag shouldBe "google"
    reportTags(1).tagStatus shouldBe "failed"
    reportTags(1).scenarios shouldBe allExecutionScenariosWithFeatureTags
    reportTags(1).scenariosMetrics shouldBe Metrics(1, 0, 1, hasScenarios = true)

    reportTags(2).tag shouldBe "complete"
    reportTags(2).tagStatus shouldBe "failed"
    reportTags(2).scenarios shouldBe allExecutionScenariosWithFeatureTags
    reportTags(2).scenariosMetrics shouldBe Metrics(1, 0, 1, hasScenarios = true)
  }

  it should "have a distinct list of all the tags without @" in {
    val reportTags = TagProcessor(features)._1
    reportTags.map(rt => rt.tag) shouldBe List("performance", "google", "complete")
  }

  behavior of "ReportTag"

  it should "be FAILED if tag contains failed scenarios" in {
    val reportTags: List[ReportTag] = TagProcessor(features)._1
    reportTags.head.tagStatus shouldBe "failed"
    reportTags(1).tagStatus shouldBe "failed"
    reportTags(2).tagStatus shouldBe "failed"
  }

  it should "be PASSED if all tag scenarios are passed" in {
    val reportTags: List[ReportTag] = TagProcessor.apply(features)._1
    pending
  }

}
