package dcs.common

import java.io._

class TaskResponseProtocol(in: InputStream, override protected val out: OutputStream)
    extends ProtocolWithIdentification {
  def sendAnswer(taskID: Int, answer: Serializable) {
    initiateCommunication(TaskResponseProtocol.id)

    val dataOut = new DataOutputStream(out)
    dataOut.writeInt(taskID)

    val baos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(baos)
    try {
      oos.writeObject(answer)
    } finally {
      oos.close()
    }
    dataOut.writeInt(baos.size)
    dataOut.write(baos.toByteArray)
  }

  def getAnswer: (Int, Serializable) = {
    val dataIn = new DataInputStream(in)
    val taskID = dataIn.readInt()

    val objectBytes = new Array[Byte](dataIn.readInt())
    dataIn.readFully(objectBytes)

    val ois = new ObjectInputStream(new ByteArrayInputStream(objectBytes))
    try {
      (taskID, ois.readObject().asInstanceOf[Serializable])
    } finally {
      ois.close()
    }
  }
}

object TaskResponseProtocol {
  val id = "task-response"
}