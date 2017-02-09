package io.magentys.donut.transformers.cucumber

import java.io.File

import io.magentys.donut.DonutTestData
import io.magentys.donut.gherkin.model
import io.magentys.donut.gherkin.model.Metrics
import io.magentys.donut.gherkin.processors.JSONProcessor
import org.scalatest.{FlatSpec, Matchers}

class GroupByFeatureTest extends FlatSpec with Matchers {

  val rootDir = List("src", "test", "resources", "samples-4").mkString("", File.separator, File.separator)
  val values = JSONProcessor.loadFrom(new File(rootDir))
  val features = CucumberTransformer.transform(values, DonutTestData.statusConfiguration)

  behavior of "CucumberAdaptor"

  it should "group donut features by feature name while transforming the list of cucumber features" in {
    features.size shouldBe 1
    features(0).name shouldBe "Add numbers"
    features(0).scenarios.size shouldBe 3
  }


}


