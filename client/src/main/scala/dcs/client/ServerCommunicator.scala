package dcs.client

import java.net.ServerSocket
import dcs.common.Constants
import java.io.IOException
import java.util.concurrent.Executors

class ServerCommunicator(computeService: DistributedComputeService) extends Runnable {
  private[this] val executor = Executors.newCachedThreadPool()
  
  def run() {
    val listener = new ServerSocket(Constants.PORT)
    try {
      while (true) {
        val socket = listener.accept()
        executor.submit(new ServerCommunicatorThread(socket, computeService))
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