package io.magentys.donut.gherkin.model

import org.joda.time.format.PeriodFormatterBuilder
import org.joda.time.{DateTime, Period}


case class Duration(duration: Long, durationStr:String)

object Duration {

  def apply(duration:Long): Duration = {
    Duration(duration, calculateTotalDurationStr(duration))
  }

  def calculateTotalDuration(durations: List[Long]) = durations.sum

  def calculateTotalDurationStr(durations: List[Long]): String = formatDuration(calculateTotalDuration(durations))
  def calculateTotalDurationStr(duration: Long): String = formatDuration(duration)

  def formatDuration(duration: Long) = {
    val formatter = new PeriodFormatterBuilder()
      .appendDays()
      .appendSuffix(" day", " days")
      .appendSeparator(" and ")
      .appendHours()
      .appendSuffix(" hour", " hours")
      .appendSeparator(" and ")
      .appendMinutes()
      .appendSuffix(" min", " mins")
      .appendSeparator(" and ")
      .appendSeconds()
      .appendSuffix(" sec", " secs")
      .appendSeparator(" and ")
      .appendMillis()
      .appendSuffix(" ms", " ms")
      .toFormatter
    formatter.print(new Period(0, duration / 1000000))
  }

  def calculateDurationFrom2Strings(startDateTime: String, endDateTime: String): Long = {
    val start = DateTime.parse(startDateTime).getMillis
    val end = DateTime.parse(endDateTime).getMillis
    (end - start) * 1000000
  }
}

