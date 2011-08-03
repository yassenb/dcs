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
                     = TaskExecutor.execute(_, new NetworkClassLoader),
                   executor: InterruptibleExecutor = new InterruptibleExecutor) {
  def poll() {
    SocketContext { (is, os) =>
      createPingProtocol(is, os).requestTimeTillNextPing match {
        case 0 => executeRemoteTask(Thread.currentThread())
        case s => {
          try {
            TimeUnit.SECONDS.sleep(s)
          } catch {
            case ie: InterruptedException => // do nothing, this must be caused by a completed task
          }
        }
      }
    }
  }

  private def executeRemoteTask(t: Thread) {
    SocketContext { (is, os) =>
      val (taskID, objectBytes) = createTaskRequestProtocol(is, os).requestTask()
      
      executor.submit({
        val answer = executeTask(objectBytes)
        SocketContext { (is, os) =>
          createTaskResponseProtocol(is, os).sendAnswer(taskID, answer)
        }
        t.interrupt()
      })
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
