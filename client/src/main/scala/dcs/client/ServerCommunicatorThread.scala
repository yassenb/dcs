package dcs.client

import java.lang.Thread
import java.net.Socket
import java.io._
import dcs.common._
import java.util.{UUID, Random}

class ServerCommunicatorThread(socket: Socket) extends Thread with Logging {
  override def run() {
    try {
      val in = new DataInputStream(socket.getInputStream)
      val serverID = new UUID(in.readLong(), in.readLong())
      in.readUTF() match {
        case PingProtocol.id =>
          logger.debug("got ping")
          val x = (new Random()).nextInt(4)
          if (x > 0) {
            logger.debug("responding with wait %d seconds".format(x))
            (new PingProtocol(socket.getInputStream, socket.getOutputStream)).respondTimeTillNextPing(x)
          } else {
            logger.debug("responding with task")
            val t = new Task[java.lang.Double] {
              def execute(): java.lang.Double = 0.5
            }
            (new PingProtocol(socket.getInputStream, socket.getOutputStream))
              .respondTask(RequestedTask(1, getObjectBytes(t)))
          }
        case TaskResponseProtocol.id =>
          logger.debug("got task response")
          val (taskID, answer) = (new TaskResponseProtocol(socket.getInputStream, socket.getOutputStream)).getAnswer
          answer.asInstanceOf[Double]
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
    val classRoot = "/home/yassen/projects/thesis/dcs/client/target/classes"
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