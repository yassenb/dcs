package dcs.common

import concurrent.Lock

object LockContext {
  def apply[T <: Any](lock: Lock)(block: T): T = {
    try {
      lock.acquire()
      block
    } finally {
      lock.release()
    }
  }
}