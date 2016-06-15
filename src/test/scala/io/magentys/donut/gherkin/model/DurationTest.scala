package io.magentys.donut.gherkin.model

import org.scalatest.{FlatSpec, Matchers}

class DurationTest extends FlatSpec with Matchers {

  behavior of "Duration Calculator "

  lazy val duration_1 = 1708141L
  lazy val duration_2 = 0L

  "A step duration" should "be presented in human readable format" in {
    Duration.formatDuration(duration_1) should be ("1 ms")
    Duration.formatDuration(duration_2) should be ("0 ms")
  }

}
