package dcs.server

import dcs.common.ClassRequestProtocol

class NetworkClassLoader() extends ClassLoader {
  override def findClass(name: String): Class[_] = {
    SocketContext { (is, os) =>
      val bytes = (new ClassRequestProtocol(is, os)).requestClassBytes(name)
      defineClass(name, bytes, 0, bytes.length)
    }
  }
}