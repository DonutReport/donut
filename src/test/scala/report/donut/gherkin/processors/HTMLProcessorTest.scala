package report.donut.gherkin.processors

import org.scalatest.{FlatSpec, Matchers}
import report.donut.gherkin.model._

class HTMLProcessorTest extends FlatSpec with Matchers {

  behavior of "HTMLProcessor"

  it should "include output from Scenario before hooks, steps and after hooks" in {
    val beforeHook = BeforeHook(List("before hook output"))
    val step = Step("a step", "Given", List.empty, List("step output"), start_time = 0L, end_time = 0L)
    val afterHook = AfterHook(List("after hook output"))
    val scenario = Scenario(Some("Test something"), "Test 1", "Scenario", List("@complete"), List(step), "", "", Status(true, ""), background = None, `type` = None, examples = List.empty, before = List(beforeHook), after = List(afterHook))
    HTMLProcessor.scenarios(scenario, "1001", "feature") should (include("before hook output") and include("step output") and include("after hook output"))
  }

  it should "include output from Background steps" in {
    val beforeHook = BeforeHook(List("before hook output"))
    val backgroundStep = Step("a step", "Given", List.empty, List("step output"), start_time = 0L, end_time = 0L)
    val step = Step("a background step", "Given", List.empty, List("background step output"), start_time = 0L, end_time = 0L)
    val afterHook = AfterHook(List("after hook output"))
    val background = Scenario(Some("Background"), "Test 1", "Scenario", List.empty, List(backgroundStep), "", "", Status(true, ""), background = None, `type` = None, examples = List.empty)
    val scenario = Scenario(Some("Test something"), "Test 1", "Scenario", List("@complete"), List(step), "", "", Status(true, ""), background = Some(background), `type` = None, examples = List.empty, before = List(beforeHook), after = List(afterHook))

    HTMLProcessor.scenarios(scenario, "1001", "feature") should (include("before hook output") and include("background step output") and include("step output") and include("after hook output"))
  }

}
