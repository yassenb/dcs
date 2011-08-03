package dcs.server

import java.io.{OutputStream, InputStream}
import dcs.common.{PingProtocol, TaskResponseProtocol, TaskRequestProtocol}
import java.util.concurrent.TimeUnit

class ClientPoller(createPingProtocol: (InputStream, OutputStream) => PingProtocol
                     = new PingProtocol(_, _),
                   createTaskRequestProtocol: (InputStream, OutputStream) => TaskRequestProtocol
                     = new TaskRequestProtocol(_, _),
                   createTaskResponseProtocol: (InputStream, OutputStream) => TaskResponseProtocol
                     = new TaskResponseProtocol(_, _),
                   executeTask: Array[Byte] => java.io.Serializable
                     = TaskExecutor.execute(_, new NetworkClassLoader)) {
  def poll() {
    SocketContext { (is, os) =>
      createPingProtocol(is, os).requestTimeTillNextPing match {
        case 0 => executeRemoteTask()
        case s => TimeUnit.SECONDS.sleep(s)
      }
    }
  }

  private def executeRemoteTask() {
    // TODO check if task already running and isolate executeTask below in a cancellable call passing callback to send
    // answer when finished
    SocketContext { (is, os) =>
      val (taskID, objectBytes) = createTaskRequestProtocol(is, os).requestTask()
      val answer = executeTask(objectBytes)

      SocketContext { (is, os) =>
        createTaskResponseProtocol(is, os).sendAnswer(taskID, answer)
      }
    }
  }
}

object ClientPoller {
  def main(args: Array[String]) {
    val cp = new ClientPoller()
    while (true) {
      cp.poll()
    }
  }
}
