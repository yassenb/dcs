package dcs.client

import dcs.common.ClosableContext
import java.io._

object ObjectToBytes {
  def getClassBytes(name: String): Array[Byte] = {
    // TODO hard-coded path
    val classRoot = "/home/yassen/projects/thesis/dcs/test_client/target/classes"
    val fileName = classRoot + File.separatorChar + name.replace('.', File.separatorChar) + ".class"
    val inFile = new FileInputStream(fileName)
    val classBytes = new Array[Byte](inFile.available)
    inFile.read(classBytes)
    classBytes
  }

  def getObjectBytes(o: Serializable): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    ClosableContext(new ObjectOutputStream(baos)){(oos) =>
      oos.writeObject(o)
    }

    baos.toByteArray
  }
}