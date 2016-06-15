package io.magentys.donut.performance

import com.codahale.metrics.MetricRegistry
import io.magentys.donut.log.Log

import scala.concurrent.duration._

trait PerformanceSupport extends Log {
  def registry: MetricRegistry = PerformanceSupport.registry

  def timed[T](id: String, msg: String)(thunk: => T): T = {
    val timer = registry.timer(id).time
    val result = thunk
    val duration = timer.stop.nanos
    log.debug(s"$msg in ${duration.toMillis}ms")
    result
  }
}

object PerformanceSupport {
  val registry = new MetricRegistry
}