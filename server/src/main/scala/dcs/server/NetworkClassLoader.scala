package dcs.server

import dcs.common.ClassRequestProtocol
import java.util.UUID

class NetworkClassLoader(getAddresses: => Addresses, getID: => UUID) extends ClassLoader {
  override def findClass(name: String): Class[_] = {
    SocketContext(getAddresses) { (is, os) =>
      val bytes = (new ClassRequestProtocol(is, os)).requestClassBytes(getID, name)
      defineClass(name, bytes, 0, bytes.length)
    }
  }
}