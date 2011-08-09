package dcs.server

import java.io.{OutputStream, InputStream}
import java.util.concurrent.TimeUnit
import scala.concurrent.ops._
import dcs.common.{Logging, PingProtocol, TaskResponseProtocol, TaskRequestProtocol}
import actors.Channel
import actors.Actor._

class ClientPoller(createPingProtocol: (InputStream, OutputStream) => PingProtocol,
                   createTaskRequestProtocol: (InputStream, OutputStream) => TaskRequestProtocol,
                   createTaskResponseProtocol: (InputStream, OutputStream) => TaskResponseProtocol,
                   executeTask: Array[Byte] => java.io.Serializable,
                   executor: InterruptibleExecutor,
                   applicationState: ApplicationState) extends Logging {
  def startPolling() {
    actor {
      case object Wake

      while (true) {
        // creating a new channel so that any messages to the old one will be discarded
        val channel = new Channel[Any]
        val wake = () => channel ! Wake
        try {
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

          channel.receive {
            case Wake =>
          }
        } catch {
          // TODO more precise error handling
          case e: Exception =>
            logger.error(e.getMessage, e)
            // TODO change value
            TimeUnit.SECONDS.sleep(2)
        }
      }
    }
  }

  private def executeRemoteTask(signalFinish: () => Unit) {
    SocketContext(applicationState.getAddresses) { (is, os) =>
      val (taskID, objectBytes) = createTaskRequestProtocol(is, os).requestTask()
      
      executor.submit({
        val answer = executeTask(objectBytes)
        SocketContext(applicationState.getAddresses) { (is, os) =>
          createTaskResponseProtocol(is, os).sendAnswer(taskID, answer)
        }
        signalFinish()
      })
    }
  }
}
