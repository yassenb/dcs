package dcs.common

import java.io.{DataInputStream, DataOutputStream, OutputStream, InputStream}

class PingProtocol(in: InputStream, override protected val out: OutputStream) extends ProtocolWithIdentification {
  def requestTimeTillNextPing: Int = {
    initiateCommunication(PingProtocol.id)
    
    val dataIn = new DataInputStream(in)
    dataIn.readInt()
  }

  def respondTimeTillNextPing(seconds: Int) {
    val dataOut = new DataOutputStream(out)
    dataOut.writeInt(seconds)
  }
}

object PingProtocol {
  val id = "ping"
}