package dcs.server

import java.io.{OutputStream, InputStream}
import java.util.concurrent.TimeUnit
import actors.Actor
import scala.concurrent.ops._
import dcs.common.{Logging, PingProtocol, TaskResponseProtocol, TaskRequestProtocol}

class ClientPoller(createPingProtocol: (InputStream, OutputStream) => PingProtocol,
                   createTaskRequestProtocol: (InputStream, OutputStream) => TaskRequestProtocol,
                   createTaskResponseProtocol: (InputStream, OutputStream) => TaskResponseProtocol,
                   executeTask: Array[Byte] => java.io.Serializable,
                   executor: InterruptibleExecutor,
                   applicationState: ApplicationState) extends Logging {
  def startPolling() {
    class Poller extends Actor {
      def act() {
        try {
          case object Wake
          val wake = () => this ! Wake

          var shouldBeSleeping = false
          while (!shouldBeSleeping) {
            SocketContext(applicationState.getAddresses) { (is, os) =>
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
        } catch {
          // TODO more precise error handling
          case e: Exception => logger.error(e.getMessage, e)

          // TODO change value
          TimeUnit.SECONDS.sleep(2)
          (new Poller).start()
        }
      }
    }
    
    (new Poller).start()
  }

  private def executeRemoteTask(signalFinish: () => Unit) {
    SocketContext(applicationState.getAddresses) { (is, os) =>
      val (taskID, objectBytes) = createTaskRequestProtocol(is, os).requestTask()
      
      executor.submit({() =>
        val answer = executeTask(objectBytes)
        SocketContext(applicationState.getAddresses) { (is, os) =>
          createTaskResponseProtocol(is, os).sendAnswer(taskID, answer)
        }
        signalFinish()
      })
    }
  }
}
