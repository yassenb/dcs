package dcs.server

import java.io.{ObjectStreamClass, InputStream, ObjectInputStream, ByteArrayInputStream}
import dcs.common.Task

object TaskExecutor {
  def execute(objectBytes: Array[Byte], classLoader: ClassLoader): java.io.Serializable = {
    createObject(objectBytes, classLoader).execute()
  }

  private def createObject(bytes: Array[Byte], classLoader: ClassLoader): Task[Serializable] = {
    class CustomClassLoaderOIS(is: InputStream) extends ObjectInputStream(is) {
      override def resolveClass(desc: ObjectStreamClass): Class[_] = {
        Class.forName(desc.getName, false, classLoader)
      }
    }

    val ois = new CustomClassLoaderOIS(new ByteArrayInputStream(bytes))
    try {
      ois.readObject().asInstanceOf[Task[Serializable]]
    } finally {
      ois.close()
    }
  }
}