package dcs.server

import java.io.{OutputStream, InputStream}
import java.net.{InetAddress, Socket}

object SocketContext {
  def apply[T](remoteAddress: String, port: Int, localAddress: String)(f: (InputStream, OutputStream) => T): T = {
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

  def apply[T](tup: (String, Int, String))(f: (InputStream, OutputStream) => T): T = {
    (apply: (String, Int, String) => ((InputStream, OutputStream) => T) => T).tupled(tup)(f)
  }
}