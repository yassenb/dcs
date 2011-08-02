package dcs.common

import java.io.{OutputStream, DataOutputStream}

abstract class ProtocolWithIdentification {
  val out: OutputStream

  protected def initiateCommunication(id: String) {
    val dataOut = new DataOutputStream(out)
    // TODO replace with randomly generated at installation UUID
    dataOut.writeInt(1)
    dataOut.writeUTF(id)
  }
}