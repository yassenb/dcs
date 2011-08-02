package dcs.server

import java.io.{OutputStream, InputStream}
import dcs.common.Constants
import java.net.{InetAddress, Socket}

object SocketContext {
  def apply[T <: Any](f: (InputStream, OutputStream) => T): T = {
    // TODO change to configured host
    val socket = new Socket(InetAddress.getByName("localhost"), Constants.PORT)
    try {
      f(socket.getInputStream, socket.getOutputStream)
    } finally {
      socket.close()
    }
  }
}