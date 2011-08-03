package dcs.common

import org.apache.log4j.Logger

trait Logging {
  lazy val logger = Logger.getLogger(this.getClass.getName)
}