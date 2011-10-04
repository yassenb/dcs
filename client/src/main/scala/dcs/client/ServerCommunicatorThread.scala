package dcs.client

import java.net.Socket
import java.io._
import dcs.common._
import java.util.UUID
import actors.Actor._

case class IdentifiableTask(task: Task[Serializable], id: Int)

class ServerCommunicatorThread(socket: Socket, computeService: DistributedComputeService)
    extends Runnable with Logging {
  override def run() {
    try {
      val in = new DataInputStream(socket.getInputStream)
      val serverID = new UUID(in.readLong(), in.readLong())
      in.readUTF() match {
        case PingProtocol.id =>
          logger.debug("got ping")
          computeService ! Ping(serverID)
          receive {
            case seconds: Int =>
              logger.debug("responding with wait %d seconds".format(seconds))
              (new PingProtocol(socket.getInputStream, socket.getOutputStream)).respondTimeTillNextPing(seconds)
            case IdentifiableTask(task, id) =>
              logger.debug("responding with task")
              (new PingProtocol(socket.getInputStream, socket.getOutputStream))
                .respondTask(RequestedTask(id, ObjectToBytes.getObjectBytes(task)))
          }
        case TaskResponseProtocol.id =>
          logger.debug("got task response")
          val (taskID, answer) = (new TaskResponseProtocol(socket.getInputStream, socket.getOutputStream)).getAnswer
          computeService ! Answer(taskID, answer)
        case ClassRequestProtocol.id =>
          (new ClassRequestProtocol(socket.getInputStream, socket.getOutputStream))
            .respondClassBytes(ObjectToBytes.getClassBytes(_, DistributedComputeService.getClientClassPath.get))
        case id: String => throw new Exception("error: unknown id " + id)
        case _ => throw new Exception("communication error")
      }
    } catch {
      case ioe: IOException =>
        // TODO handle error
        logger.error(ioe.getMessage, ioe)
      case e: Exception =>
        // TODO handle error
        logger.error(e.getMessage, e)
    } finally {
      socket.close()
    }
  }
}