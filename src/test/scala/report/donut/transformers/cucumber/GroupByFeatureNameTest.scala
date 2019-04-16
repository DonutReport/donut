package report.donut.transformers.cucumber

import java.io.File

import report.donut.DonutTestData
import report.donut.gherkin.model
import report.donut.gherkin.processors.JSONProcessor
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ListBuffer

class GroupByFeatureNameTest extends FlatSpec with Matchers{

  // BDD json files for same feature
  private val sample4RootDir = List("src", "test", "resources", "samples-4").mkString("", File.separator, File.separator)
  private val sample4Values = JSONProcessor.loadFrom(new File(sample4RootDir))
  private val sample4Features = CucumberTransformer.transform(sample4Values.right.get, new ListBuffer[model.Feature], DonutTestData.statusConfiguration).right.get

  // Unit tests as BDD format json files
  private val sample5RootDir = List("src", "test", "resources", "samples-5").mkString("", File.separator, File.separator)
  private val sample5Values = JSONProcessor.loadFrom(new File(sample5RootDir))

  // BDD and Unit test json files in BDD format, but with different feature names
  private val sample6BDDRootDir = List("src", "test", "resources", "samples-6","bdd").mkString("", File.separator, File.separator)
  private val sample6BDDValues = JSONProcessor.loadFrom(new File(sample6BDDRootDir))
  private val sample6BDDFeatures = CucumberTransformer.transform(sample6BDDValues.right.get, new ListBuffer[model.Feature], DonutTestData.statusConfiguration).right.get

  private val sample6UnitTestsRootDir = List("src", "test", "resources", "samples-6","unit").mkString("", File.separator, File.separator)
  private val sample6UnitTestsValues = JSONProcessor.loadFrom(new File(sample6UnitTestsRootDir))

  behavior of "Cucumber transformer - Group by feature name"

  it should "group donut features by feature name while transforming the list of cucumber features" in {
    sample4Features.size shouldBe 1
    sample4Features.head.name shouldBe "Add numbers"

    val expectedScenarioNames = List("Add two numbers: 1 and 2", "Only 1 number is provided", "Add four numbers: 1,2,5,10")
    val scenarios = sample4Features.head.scenarios
    scenarios.size shouldBe 3
    scenarios.map(s => s.name).sorted shouldBe expectedScenarioNames.sorted
  }

  it should "mapToDonutFeatures if a feature is split across multiple BDD json files" in {
    val originalFeatures: List[Feature] = CucumberTransformer.loadCukeFeatures(sample4Values.right.get)
    val generatedFeatures = CucumberTransformer.mapToDonutFeatures(originalFeatures, new ListBuffer[model.Feature], DonutTestData.statusConfiguration)
    val scenarios = generatedFeatures.head.scenarios

    originalFeatures.size shouldBe 3
    generatedFeatures.size shouldBe 1
    scenarios.size shouldBe 3
    generatedFeatures.head.index.toInt shouldBe 10000


    for (o <- originalFeatures) {
      o.name == generatedFeatures.head.name
    }
  }

  it should "mapToDonutFeatures if 1 feature is split across few BDD and unit test json files" in {
    val originalNonBDDFeatures: List[Feature] = CucumberTransformer.loadCukeFeatures(sample5Values.right.get)
    val generatedFeatures = CucumberTransformer.mapToDonutFeatures(originalNonBDDFeatures, sample4Features, DonutTestData.statusConfiguration)
    val scenarios = generatedFeatures.head.scenarios

    generatedFeatures.size shouldBe 1
    scenarios.size shouldBe 4
    scenarios(3).keyword shouldBe "Unit Test"
    scenarios(3).name should equal(originalNonBDDFeatures.head.elements.head.name)
  }

  it should "mapToDonutFeatures when there are few bdd json files and few unit test json files with a different feature name" in {
    val originalNonBDDFeatures: List[Feature] = CucumberTransformer.loadCukeFeatures(sample6UnitTestsValues.right.get)
    val generatedFeatures = CucumberTransformer.mapToDonutFeatures(originalNonBDDFeatures, sample6BDDFeatures, DonutTestData.statusConfiguration)
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
