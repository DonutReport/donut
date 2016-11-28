package io.magentys.donut.template

import io.magentys.donut.DonutTestData
import io.magentys.donut.gherkin.model.{ProjectMetadata, Report}
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}

class TemplateEngineTest extends FlatSpec with Matchers {

  val features = DonutTestData.features_sample_2;
  val report = Report(features, DateTime.now, ProjectMetadata())

  behavior of "Template Engine"

  it should "bind the data to the mustache template" in {
    val boundTemplate = TemplateEngine(report, "/template/index.html").boundTemplate.trim
    boundTemplate shouldEqual "<p>Google Journey Performance</p>"
  }

  behavior of "Template Engine - SpecialCharHandler"

  it should "unescape html special chars" in {
    val input = """ boo &lt;code&gt; bla &lt;/code&gt; boo &lt;code&gt; &lt;div>bla&lt;/div&gt; bla &lt;/code&gt; boo """
    val expectedOutput = """ boo <code> bla </code> boo <code> <div>bla</div> bla </code> boo """
    val output = SpecialCharHandler.unescapeReport(input)
    output shouldBe expectedOutput
  }

  it should "escape error messages special chars if any" in {
    val input = """ boo <code> bla </code> boo <code> <div>bla</div> bla </code> boo """
    val expectedOutput = """ boo  bla  boo  &lt;div&gt;bla&lt;/div&gt; bla  boo """
    val acutalOutput = SpecialCharHandler.escapeErrorMessages(input)
    acutalOutput shouldEqual expectedOutput
  }
}
