package io.magentys.donut.log

import org.slf4j.LoggerFactory

trait Log {
  def log = LoggerFactory.getLogger(this.getClass)
}