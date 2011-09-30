package dcs.client

import java.net.Socket
import java.io._
import dcs.common._
import java.util.UUID
import actors.Actor._

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
            case task: Task[Serializable] =>
              logger.debug("responding with task")
              (new PingProtocol(socket.getInputStream, socket.getOutputStream))
                .respondTask(RequestedTask(1, getObjectBytes(task)))
          }
        case TaskResponseProtocol.id =>
          logger.debug("got task response")
          val (taskID, answer) = (new TaskResponseProtocol(socket.getInputStream, socket.getOutputStream)).getAnswer
          computeService ! Answer(taskID, answer)
        case ClassRequestProtocol.id =>
          (new ClassRequestProtocol(socket.getInputStream, socket.getOutputStream)).respondClassBytes(getClassBytes)
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

  // TODO move these methods elsewhere
  private def getClassBytes(name: String): Array[Byte] = {
    // TODO hard-coded path
    val classRoot = "/home/yassen/projects/thesis/dcs/test_client/target/classes"
    val fileName = classRoot + File.separatorChar + name.replace('.', File.separatorChar) + ".class"
    val inFile = new FileInputStream(fileName)
    val classBytes = new Array[Byte](inFile.available)
    inFile.read(classBytes)
    classBytes
  }

  private def getObjectBytes(o: Serializable): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    ClosableContext(new ObjectOutputStream(baos)){(oos) =>
      oos.writeObject(o)
    }

    baos.toByteArray
  }
}