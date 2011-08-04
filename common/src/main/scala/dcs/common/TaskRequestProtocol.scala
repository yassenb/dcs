package dcs.common

import java.io.{OutputStream, InputStream, DataInputStream, DataOutputStream}

class TaskRequestProtocol(in: InputStream, override protected val out: OutputStream)
    extends ProtocolWithIdentification {
  // TODO think about rewriting request and response so as to avoid code duplication and enforce type safety
  def requestTask(): (Int, Array[Byte]) = {
    initiateCommunication(TaskRequestProtocol.id)
    
    val dataIn = new DataInputStream(in)
    val taskID = dataIn.readInt()

    val objectBytes = new Array[Byte](dataIn.readInt())
    dataIn.readFully(objectBytes)

    (taskID, objectBytes)
  }

  def respondTask(taskID: Int, objectBytes: Array[Byte]) {
    val dataOut = new DataOutputStream(out)
    dataOut.writeInt(taskID)
    
    dataOut.writeInt(objectBytes.length)
    dataOut.write(objectBytes)
  }
}

object TaskRequestProtocol {
  val id = "task-request"
}