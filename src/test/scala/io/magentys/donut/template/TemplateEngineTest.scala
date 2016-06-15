package io.magentys.donut.template

import java.io.File

import io.magentys.donut.DonutTestData
import io.magentys.donut.gherkin.model.{ProjectMetadata, Report}
import org.joda.time.DateTime
import org.scalatest.{Matchers, FlatSpec}

class TemplateEngineTest extends FlatSpec with Matchers {

  val features = DonutTestData.features_sample_2;
  val report = Report(features, DateTime.now, ProjectMetadata())

  behavior of "Template Engine"

  it should "bind the data to the mustache template" in {
   // val features = JSONProcessor.loadFrom(new File("src/test/resources/samples-2"))
    pending
  }

  it should "render the final report in an HTML template" in {
//    val a = TemplateEngine(report, s"/templates/default/index.html").boundTemplate
//    a shouldEqual scala.io.Source.fromFile("src/test/resources/report/donut-report.html").mkString
    pending
  }
}
