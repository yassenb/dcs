package dcs.server

import dcs.common.{ClosableContext, Constants}
import java.util.Properties
import java.io.{FileOutputStream, FileInputStream}

class Configuration(file: String = Constants.CONFIGURATION_FILE) {
  private val (remoteAddress, localAddress) = {
    try {
      ClosableContext(new FileInputStream(file)) {(is) =>
        val props = new Properties
        props.load(is)
        (props.getProperty(Constants.PropertyNames.REMOTE_ADDRESS),
         props.getProperty(Constants.PropertyNames.LOCAL_ADDRESS))
      }
    } catch {
      case _ => ("", "")
    }
  }

  def getAddresses: Addresses = {
    Addresses(remoteAddress, Constants.PORT, localAddress)
  }
}

object Configuration {
  def save(remoteAddress: String, localAddress: String, file: String = Constants.CONFIGURATION_FILE) {
    ClosableContext(new FileOutputStream(file)) {(os) =>
      val props = new Properties
      val propPairs = List((Constants.PropertyNames.REMOTE_ADDRESS, remoteAddress),
                           (Constants.PropertyNames.LOCAL_ADDRESS, localAddress))
      for ((k, v) <- propPairs) {
        props.setProperty(k, v)
      }
      props.store(os, null)
    }
  }
}