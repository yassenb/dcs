package dcs.server

import java.io.{OutputStream, InputStream}
import java.util.concurrent.TimeUnit
import dcs.common.{PingProtocol, TaskResponseProtocol, TaskRequestProtocol}
import actors.Actor
import scala.concurrent.ops._

class ClientPoller(createPingProtocol: (InputStream, OutputStream) => PingProtocol
                     = new PingProtocol(_, _),
                   createTaskRequestProtocol: (InputStream, OutputStream) => TaskRequestProtocol
                     = new TaskRequestProtocol(_, _),
                   createTaskResponseProtocol: (InputStream, OutputStream) => TaskResponseProtocol
                     = new TaskResponseProtocol(_, _),
                   executeTask: Array[Byte] => java.io.Serializable
                     = TaskExecutor.execute(_, new NetworkClassLoader),
                   executor: InterruptibleExecutor = new InterruptibleExecutor,
                   applicationState: ApplicationState = new ApplicationState) {
  def startPolling() {
    class Poller extends Actor {
      def act() {
        case object Wake
        val wake = () => this ! Wake

        var shouldBeSleeping = false
        while (!shouldBeSleeping) {
          SocketContext { (is, os) =>
            createPingProtocol(is, os).requestTimeTillNextPing match {
              case 0 => executeRemoteTask(wake)
              case s => {
                shouldBeSleeping = true
                spawn {
                  TimeUnit.SECONDS.sleep(s)
                  wake()
                }
              }
            }
          }
        }

        receive {
          case Wake =>
        }

        // spawning a new actor so that any leftover wake up messages get discarded
        // this could maybe be a message to another actor and leave the job of spawning to it
        (new Poller).start()
      }
    }
    
    (new Poller).start()
  }

  private def executeRemoteTask(signalFinish: () => Unit) {
    SocketContext { (is, os) =>
      val (taskID, objectBytes) = createTaskRequestProtocol(is, os).requestTask()
      
      executor.submit({() =>
        val answer = executeTask(objectBytes)
        SocketContext { (is, os) =>
          createTaskResponseProtocol(is, os).sendAnswer(taskID, answer)
        }
        signalFinish()
      })
    }
  }
}
