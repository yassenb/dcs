package dcs.server

import java.io.{OutputStream, InputStream}
import java.net.{InetAddress, Socket}

object SocketContext {
  def apply[T](addresses: Addresses)(f: (InputStream, OutputStream) => T): T = {
    val socket = new Socket(InetAddress.getByName(addresses.remoteAddress),
                            addresses.port,
                            if (addresses.localAddress != null) InetAddress.getByName(addresses.localAddress) else null,
                            0)
    try {
      f(socket.getInputStream, socket.getOutputStream)
    } finally {
      socket.close()
    }
  }
}