package dcs.common

import java.io.{OutputStream, DataOutputStream}
import java.util.UUID

abstract class ProtocolWithIdentification {
  protected val out: OutputStream

  protected def initiateCommunication(serverID: UUID, protocolID: String) {
    val dataOut = new DataOutputStream(out)
    dataOut.writeLong(serverID.getMostSignificantBits)
    dataOut.writeLong(serverID.getLeastSignificantBits)
    dataOut.writeUTF(protocolID)
  }
}