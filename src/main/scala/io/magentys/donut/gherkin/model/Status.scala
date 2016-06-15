package io.magentys.donut.gherkin.model

case class Status(status:Boolean, statusStr:String)

object Status {

  def apply(conf: StatusConfiguration, statusStr: String): Status = {
    Status(calculate(conf, statusStr), statusStr)
  }

  val PASSED = "passed"
  val FAILED = "failed"
  val SKIPPED = "skipped"
  val PENDING = "pending"
  val UNDEFINED = "undefined"
  val MISSING = "missing"

  def calculate(conf: StatusConfiguration, statuses: List[String]): Boolean = {
    if (statuses.contains(Status.FAILED)) false
    else if (conf.countSkippedAsFailure & statuses.contains(Status.SKIPPED)) false
    else if (conf.countPendingAsFailure & statuses.contains(Status.PENDING)) false
    else if (conf.countUndefinedAsFailure & statuses.contains(Status.UNDEFINED)) false
    else if (conf.countMissingAsFailure & statuses.contains(Status.MISSING)) false
    else true
  }

  def calculate(conf: StatusConfiguration, statusStr: String): Boolean = {
    if (statusStr == Status.FAILED) false
    else if (conf.countSkippedAsFailure & statusStr == Status.SKIPPED) false
    else if (conf.countPendingAsFailure & statusStr == Status.PENDING) false
    else if (conf.countUndefinedAsFailure & statusStr == Status.UNDEFINED) false
    else if (conf.countMissingAsFailure & statusStr == Status.MISSING) false
    else true
  }

}

case class StatusConfiguration(countSkippedAsFailure: Boolean = false,
                               countPendingAsFailure: Boolean = false,
                               countUndefinedAsFailure: Boolean = false,
                               countMissingAsFailure: Boolean = false)
