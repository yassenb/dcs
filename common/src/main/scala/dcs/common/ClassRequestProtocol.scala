package dcs.common

import java.io.{DataInputStream, DataOutputStream, OutputStream, InputStream}

class ClassRequestProtocol(in: InputStream, override val out: OutputStream) extends ProtocolWithIdentification {
  def requestClassBytes(name: String): Array[Byte] = {
    initiateCommunication(ClassRequestProtocol.id)
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