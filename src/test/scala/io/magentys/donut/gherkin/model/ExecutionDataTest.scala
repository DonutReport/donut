package io.magentys.donut.gherkin.model

import io.magentys.donut.DonutTestData
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}

class ExecutionDataTest  extends FlatSpec with Matchers {

  behavior of "ExecutionData"

  val features = DonutTestData.features_sample_3;

  val timestamp = DateTime.now
  val executionData = ExecutionData(features, timestamp)

  it should "give the combined scenarios for an execution" in {
    val expectedScenarios = List("Google Journey Performance", "Click on element with offset")
    executionData.allScenarios.map(s => s.name) shouldBe expectedScenarios
    ExecutionData.allScenarios(features).map(s => s.name) shouldBe expectedScenarios
  }

  it should "give the combined steps for an execution" in {
    val expectedSteps = List(
      "I run the google journey test for 7000 milliseconds",
      "the average journey should not exceed the 3000 milliseconds threshold",
      "I have navigated to the local page \"/absolute.html\"",
      "I choose to click button \"b1\" with offset (110,0)",
      "I should see \"You clicked button: B2\"")

    ExecutionData.allSteps(features).map(f => f.name) shouldBe expectedSteps
  }

  it should "give the combined tags for an execution" in {
    val expectedTags = List("google", "performance", "local", "complete")
    ExecutionData.allTags(features) shouldBe expectedTags
  }

  it should "give the failures for an execution" in {
    val expectedScenarios = List("Google Journey Performance")
    executionData.allFailures.map(s => s.name) shouldBe expectedScenarios
    ExecutionData.allFailures(features).map(s => s.name) shouldBe expectedScenarios
  }
}