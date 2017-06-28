package io.magentys.donut.template

import io.magentys.donut.DonutTestData
import io.magentys.donut.gherkin.model.{ProjectMetadata, Report}
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}

class TemplateEngineTest extends FlatSpec with Matchers {

  val features = DonutTestData.features_sample_2.right.get;
  val report = Report(features, DateTime.now, ProjectMetadata("", "", Map()))

  behavior of "Template Engine"

  it should "bind the data to the mustache template" in {
    val boundTemplate = TemplateEngine(report, "/template/index.html").boundTemplate.trim
    boundTemplate shouldEqual "<p>Google Journey Performance</p>"
  }

  behavior of "Template Engine - SpecialCharHandler"

  it should "escape the < and > chars if any" in {
    val input = """ boo  "<bla>" & 1>0"""
    val expectedOutput = """ boo  "&lt;bla&gt;" & 1&gt;0"""
    val actualOutput = SpecialCharHandler.escape(input)
    actualOutput shouldEqual expectedOutput
  }
}
