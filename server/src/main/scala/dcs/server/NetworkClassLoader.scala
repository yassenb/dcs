package dcs.server

import java.net.{InetAddress, Socket}
import dcs.common.{ClassRequestProtocol, Constants}

class NetworkClassLoader() extends ClassLoader {
  override def findClass(name: String): Class[_] = {
    // TODO isolate socket creation in a util class
    val socket = new Socket(InetAddress.getByName("localhost"), Constants.PORT)
    val bytes = (new ClassRequestProtocol(socket.getInputStream, socket.getOutputStream)).requestClassBytes(name)

    defineClass(name, bytes, 0, bytes.length)
  }
}