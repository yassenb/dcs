package dcs.server

import java.net.{InetAddress, Socket}
import dcs.common.{ClassRequestProtocol, TaskResponseProtocol, TaskRequestProtocol, Constants}

object App {
  def main(args: Array[String]) {
    SocketContext { (is, os) =>
      val (taskID, objectBytes) =
        (new TaskRequestProtocol(is, os)).requestTask()
      val cl = new NetworkClassLoader
      val answer = TaskExecutor.execute(objectBytes, cl)

      SocketContext { (is, os) =>
        (new TaskResponseProtocol(is, os)).sendAnswer(taskID, answer)
      }
    }
  }
}
