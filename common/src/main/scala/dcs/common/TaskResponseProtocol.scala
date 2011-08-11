package dcs.common

import java.io._

class TaskResponseProtocol(in: InputStream, override protected val out: OutputStream)
    extends ProtocolWithIdentification {
  def sendAnswer(taskID: Int, answer: Serializable) {
    initiateCommunication(TaskResponseProtocol.id)

    val dataOut = new DataOutputStream(out)
    dataOut.writeInt(taskID)

    val baos = new ByteArrayOutputStream()
    ClosableContext(new ObjectOutputStream(baos)){(oos) =>
      oos.writeObject(answer)
    }
    dataOut.writeInt(baos.size)
    dataOut.write(baos.toByteArray)
  }

  def getAnswer: (Int, Serializable) = {
    val dataIn = new DataInputStream(in)
    val taskID = dataIn.readInt()

    val objectBytes = new Array[Byte](dataIn.readInt())
    dataIn.readFully(objectBytes)

    ClosableContext(new ObjectInputStream(new ByteArrayInputStream(objectBytes))){(ois) =>
      (taskID, ois.readObject().asInstanceOf[Serializable])
    }
  }
}

object TaskResponseProtocol {
  val id = "task-response"
}