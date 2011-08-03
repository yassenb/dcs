package dcs.client

import java.lang.Thread
import java.net.Socket
import java.io._
import dcs.common.{ClassRequestProtocol, TaskResponseProtocol, TaskRequestProtocol, PingProtocol}
import java.util.Random

class ServerCommunicatorThread(socket: Socket) extends Thread {
  override def run() {
    try {
      val in = new DataInputStream(socket.getInputStream)
      val serverID = in.readInt()
      in.readUTF() match {
        case PingProtocol.id =>
          // TODO change ping time
          (new PingProtocol(socket.getInputStream, socket.getOutputStream))
            .respondTimeTillNextPing((new Random()).nextInt(4))
        case TaskRequestProtocol.id =>
          val t = new SimpleTask(0.5)
          // TODO task id
          (new TaskRequestProtocol(socket.getInputStream, socket.getOutputStream)).respondTask(1, getObjectBytes(t))
        case TaskResponseProtocol.id =>
          val (taskID, answer) = (new TaskResponseProtocol(socket.getInputStream, socket.getOutputStream)).getAnswer
          println(answer.asInstanceOf[Double])
        case ClassRequestProtocol.id =>
          (new ClassRequestProtocol(socket.getInputStream, socket.getOutputStream)).respondClassBytes(getClassBytes)
        case id: String => throw new Exception("error: unknown id " + id)
        case _ => throw new Exception("communication error")
      }
    } catch {
      case ioe: IOException =>
        // TODO handle error
        println(ioe.getMessage)
      case e: Exception =>
        // TODO handle error
        println(e.getMessage)
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
    val oos = new ObjectOutputStream(baos)
    try {
      oos.writeObject(o)
    } finally {
      oos.close()
    }

    baos.toByteArray
  }
}