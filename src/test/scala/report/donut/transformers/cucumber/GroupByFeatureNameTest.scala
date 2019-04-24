package report.donut.transformers.cucumber

import java.io.File

import org.json4s.DefaultFormats
import org.scalatest.{FlatSpec, Matchers}
import report.donut.DonutTestData
import report.donut.gherkin.model
import report.donut.gherkin.processors.JSONProcessor

import scala.collection.mutable.ListBuffer

class GroupByFeatureNameTest extends FlatSpec with Matchers {

  implicit val formats = DefaultFormats

  // BDD json files for same feature
  private val sample4RootDir = List("src", "test", "resources", "samples-4").mkString("", File.separator, File.separator)
  private val sample4Features = JSONProcessor.loadFrom(new File(sample4RootDir)).right.get.flatMap(f => f.extract[List[Feature]])
  private val sample4DonutFeatures = CucumberTransformer.transform(sample4Features, DonutTestData.statusConfiguration).right.get

  // Unit tests as BDD format json files
  private val sample5RootDir = List("src", "test", "resources", "samples-5").mkString("", File.separator, File.separator)
  private val sample5Features = JSONProcessor.loadFrom(new File(sample5RootDir)).right.get.flatMap(f => f.extract[List[Feature]])

  // BDD and Unit test json files in BDD format, but with different feature names
  private val sample6BDDRootDir = List("src", "test", "resources", "samples-6", "bdd").mkString("", File.separator, File.separator)
  private val sample6BDDFeatures = JSONProcessor.loadFrom(new File(sample6BDDRootDir)).right.get.flatMap(f => f.extract[List[Feature]])
  private val sample6BDDDonutFeatures = CucumberTransformer.transform(sample6BDDFeatures, DonutTestData.statusConfiguration).right.get

  private val sample6UnitTestsRootDir = List("src", "test", "resources", "samples-6", "unit").mkString("", File.separator, File.separator)
  private val sample6UnitTests = JSONProcessor.loadFrom(new File(sample6UnitTestsRootDir)).right.get.flatMap(f => f.extract[List[Feature]])

  behavior of "Cucumber transformer - Group by feature name"

  it should "group donut features by feature name while transforming the list of cucumber features" in {
    sample4DonutFeatures.size shouldBe 1
    sample4DonutFeatures.head.name shouldBe "Add numbers"

    val expectedScenarioNames = List("Add two numbers: 1 and 2", "Only 1 number is provided", "Add four numbers: 1,2,5,10")
    val scenarios = sample4DonutFeatures.head.scenarios
    scenarios.size shouldBe 3
    scenarios.map(s => s.name).sorted shouldBe expectedScenarioNames.sorted
  }

  it should "mapToDonutFeatures if a feature is split across multiple BDD json files" in {
    val donutFeatures = CucumberTransformer.mapToDonutFeatures(sample4Features, new ListBuffer[model.Feature], DonutTestData.statusConfiguration)
    val scenarios = donutFeatures.head.scenarios

    sample4Features.size shouldBe 3
    donutFeatures.size shouldBe 1
    scenarios.size shouldBe 3
    donutFeatures.head.index.toInt shouldBe 10000

    for (o <- sample4Features) {
      o.name == donutFeatures.head.name
    }
  }

  it should "mapToDonutFeatures if 1 feature is split across few BDD and unit test json files" in {
    val generatedFeatures = CucumberTransformer.mapToDonutFeatures(sample5Features, sample4DonutFeatures, DonutTestData.statusConfiguration)
    val scenarios = generatedFeatures.head.scenarios

    generatedFeatures.size shouldBe 1
    scenarios.size shouldBe 4
    scenarios(3).keyword shouldBe "Unit Test"
    scenarios(3).name should equal(sample5Features.head.elements.head.name)
  }

  it should "mapToDonutFeatures when there are few bdd json files and few unit test json files with a different feature name" in {
    val generatedFeatures = CucumberTransformer.mapToDonutFeatures(sample6UnitTests, sample6BDDDonutFeatures, DonutTestData.statusConfiguration)
    val nonBDDFeature = generatedFeatures(1)
    val nonBDDScenario = nonBDDFeature.scenarios.head
    val bddFeature = generatedFeatures.head

    generatedFeatures.size shouldBe 2
    bddFeature.name shouldBe "Add numbers"
    bddFeature.index shouldBe "10000"
    nonBDDFeature.name shouldBe "Without feature"
    nonBDDFeature.index shouldBe "10001"
    nonBDDScenario.name shouldBe "Add four numbers: 1,2,5,10"
    nonBDDScenario.keyword shouldBe "Unit Test"
  }
}
