package dcs.common

import java.io.{DataInputStream, DataOutputStream, OutputStream, InputStream}
import java.util.UUID

class ClassRequestProtocol(in: InputStream, override protected val out: OutputStream)
    extends ProtocolWithIdentification {
  def requestClassBytes(serverID: UUID, name: String): Array[Byte] = {
    initiateCommunication(serverID, ClassRequestProtocol.id)
    val dataOut = new DataOutputStream(out)
    dataOut.writeUTF(name)

    val dataIn = new DataInputStream(in)
    val bytes = new Array[Byte](dataIn.readInt())
    dataIn.readFully(bytes)
    bytes
  }

  def respondClassBytes(getBytes: String => Array[Byte]) {
    val dataIn = new DataInputStream(in)
    val bytes = getBytes(dataIn.readUTF())
    
    val dataOut = new DataOutputStream(out)
    dataOut.writeInt(bytes.length)
    dataOut.write(bytes)
  }
}

object ClassRequestProtocol {
  val id = "class-request"
}