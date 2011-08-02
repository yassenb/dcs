package dcs.server

import java.net.{InetAddress, Socket}
import dcs.common.{ClassRequestProtocol, TaskResponseProtocol, TaskRequestProtocol, Constants}

object App {
  def main(args: Array[String]) {
    val socket = new Socket(InetAddress.getByName("localhost"), Constants.PORT)
    try {
      val (taskID, objectBytes) =
        (new TaskRequestProtocol(socket.getInputStream, socket.getOutputStream)).requestTask()
      val cl = new NetworkClassLoader
      val answer = TaskExecutor.execute(objectBytes, cl)

      val socket2 = new Socket(InetAddress.getByName("localhost"), Constants.PORT)
      try {
        (new TaskResponseProtocol(socket2.getInputStream, socket2.getOutputStream)).sendAnswer(taskID, answer)
      } finally {
        socket2.close()
      }
    } finally {
      socket.close()
    }
  }
}
