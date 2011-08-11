package dcs.server

import java.io.{OutputStream, InputStream}
import java.net.{InetAddress, Socket}

object SocketContext {
  def apply[T](tuple: (String, Int, String))(f: (InputStream, OutputStream) => T): T = {
    val (remoteAddress, port, localAddress) = tuple
    val socket = new Socket(InetAddress.getByName(remoteAddress),
                            port,
                            if (localAddress != null) InetAddress.getByName(localAddress) else null,
                            0)
    try {
      f(socket.getInputStream, socket.getOutputStream)
    } finally {
      socket.close()
    }
  }
}