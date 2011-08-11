package dcs.common

import java.io.Closeable

object ClosableContext {
  def apply[S <: Closeable, T](resource: S)(block: (S) => T): T = {
    try {
      block(resource)
    } finally {
      resource.close()
    }
  }
}