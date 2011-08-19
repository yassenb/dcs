package dcs.server

import java.io.{OutputStream, InputStream}
import java.util.concurrent.TimeUnit
import scala.concurrent.ops._
import actors.Channel
import actors.Actor._
import dcs.common.{RequestedTask, Logging, PingProtocol, TaskResponseProtocol}

class ClientPoller(createPingProtocol: (InputStream, OutputStream) => PingProtocol,
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
              val x = createPingProtocol(is, os).requestTaskOrSleepTime(applicationState.serverID)
              if (x.isLeft) {
                executeRemoteTask(x.left.get, wake)
              } else {
                shouldBeSleeping = true
                spawn {
                  TimeUnit.SECONDS.sleep(x.right.get)
                  wake()
                }
              }
            }
            applicationState.setError(None)
          }

          channel.receive {
            case Wake =>
          }
        } catch {
          // TODO more precise error handling
          case e: Exception =>
            logger.error(e.getMessage, e)
            applicationState.setError(Some(e.getMessage))
            // TODO change value
            TimeUnit.SECONDS.sleep(2)
        }
      }
    }
  }

  private def executeRemoteTask(task: RequestedTask, signalFinish: () => Unit) {
    executor.submit({
      val answer = executeTask(task.objectBytes)
      SocketContext(applicationState.getAddresses) { (is, os) =>
        createTaskResponseProtocol(is, os).sendAnswer(applicationState.serverID, task.taskID, answer)
      }
      signalFinish()
    })
  }
}
