package dcs.client

import dcs.common.Constants
import java.net.ServerSocket
import java.io.IOException

object App {
  def main(args: Array[String]) {
    val listener = new ServerSocket(Constants.PORT)
    try {
      while (true) {
        val socket = listener.accept()
        (new ServerCommunicatorThread(socket)).start()
      }
    } catch {
      case e: IOException =>
        System.err.println("Could not listen on port: " + Constants.PORT)
        System.exit(-1)
    } finally {
      listener.close()
    }
  }
}