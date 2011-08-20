package dcs.common

import java.io.{DataInputStream, DataOutputStream, OutputStream, InputStream}
import java.util.UUID

class PingProtocol(in: InputStream, override protected val out: OutputStream) extends ProtocolWithIdentification {
  // TODO think about rewriting request and response so as to avoid code duplication and enforce type safety

  def requestTaskOrSleepTime(serverID: UUID): Either[RequestedTask, Int] = {
    initiateCommunication(serverID, PingProtocol.id)
    
    val dataIn = new DataInputStream(in)
    if (dataIn.readBoolean()) {
      val taskID = dataIn.readInt()

      val objectBytes = new Array[Byte](dataIn.readInt())
      dataIn.readFully(objectBytes)

      Left(RequestedTask(taskID, objectBytes))
    } else {
      Right(dataIn.readInt())
    }
  }

  def respondTimeTillNextPing(seconds: Int) {
    val dataOut = new DataOutputStream(out)
    dataOut.writeBoolean(false)

    dataOut.writeInt(seconds)
  }

  def respondTask(task: RequestedTask) {
    val dataOut = new DataOutputStream(out)
    dataOut.writeBoolean(true)
    
    dataOut.writeInt(task.taskID)

    dataOut.writeInt(task.objectBytes.length)
    dataOut.write(task.objectBytes)
  }
}

object PingProtocol {
  val id = "ping"
}