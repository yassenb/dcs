package dcs.server

import dcs.common.ClassRequestProtocol

class NetworkClassLoader(getAddresses: => (String, Int, String)) extends ClassLoader {
  override def findClass(name: String): Class[_] = {
    SocketContext(getAddresses) { (is, os) =>
      val bytes = (new ClassRequestProtocol(is, os)).requestClassBytes(name)
      defineClass(name, bytes, 0, bytes.length)
    }
  }
}