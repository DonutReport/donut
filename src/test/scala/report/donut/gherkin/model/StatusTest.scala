package report.donut.gherkin.model

import org.scalatest.{FlatSpec, Matchers}

class StatusTest  extends FlatSpec with Matchers {

  behavior of "Scenario status"

  it should "be FAILED if it contains at least a failed step" in {
    val conf = StatusConfiguration(false, false, false, false)
    val statuses = List("passed", "failed", "skipped")
    Status.calculate(conf, statuses) shouldEqual(false)
  }

  it should "be FAILED if it contains at least a failed step regardless the config parameters" in {
    val conf = StatusConfiguration(false, false, true, false)
    val statuses = List("passed", "failed", "skipped")
    Status.calculate(conf, statuses) shouldEqual(false)
  }

  it should "be PASSED if all steps are passed regadless the parameters" in {
    val conf = StatusConfiguration(true, false, false, false)
    val statuses = List("passed", "passed", "passed")
    Status.calculate(conf, statuses) shouldEqual(true)
  }

  /* SKIPPED config */

  it should "be FAILED if countSkippedAsFailure=true and scenario contains skipped steps" in {
    val conf = StatusConfiguration(true, false, false, false)
    val statuses = List("passed", "passed", "skipped")
    Status.calculate(conf, statuses) shouldEqual(false)
  }

  it should "be PASSED if countSkippedAsFailure=false and scenario contains skipped steps" in {
    val conf = StatusConfiguration(false, false, false, false)
    val statuses = List("passed", "passed", "skipped")
    Status.calculate(conf, statuses) shouldEqual(true)
  }

  /* PENDING config */

  it should "be FAILED if countPendingAsFailure=true and scenario contains pending steps" in {
    val conf = StatusConfiguration(false, true, false, false)
    val statuses = List("passed", "passed", "pending")
    Status.calculate(conf, statuses) shouldEqual(false)
  }

  it should "be PASSED if countPendingAsFailure=false and scenario contains pending steps" in {
    val conf = StatusConfiguration(false, false, false, false)
    val statuses = List("passed", "passed", "pending")
    Status.calculate(conf, statuses) shouldEqual(true)
  }

  /* UNDEFINED config */

  it should "be FAILED if countUndefinedAsFailure=true and scenario contains undefined steps" in {
    val conf = StatusConfiguration(false, false, true, false)
    val statuses = List("passed", "passed", "undefined")
    Status.calculate(conf, statuses) shouldEqual(false)
  }

  it should "be PASSED if countUndefinedAsFailure=false and scenario contains undefined steps" in {
    val conf = StatusConfiguration(false, false, false, false)
    val statuses = List("passed", "passed", "undefined")
    Status.calculate(conf, statuses) shouldEqual(true)
  }

  /* MISSING config */

  it should "be FAILED if countMissingAsFailure=true and scenario contains missing steps" in {
    val conf = StatusConfiguration(false, false, false, true)
    val statuses = List("passed", "passed", "missing")
    Status.calculate(conf, statuses) shouldEqual(false)
  }

  it should "be PASSED if countMissingAsFailure=false and scenario contains missing steps" in {
    val conf = StatusConfiguration(false, false, false, false)
    val statuses = List("passed", "passed", "missing")
    Status.calculate(conf, statuses) shouldEqual(true)
  }
}
