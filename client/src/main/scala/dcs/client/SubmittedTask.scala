package dcs.client

import dcs.common.Task
import concurrent.SyncVar
import java.util.concurrent.Future
import java.io.Serializable

class SubmittedTask[T <: Serializable](val task: Task[T]) {
  private[this] val result = new SyncVar[T]

  def getResult: Future[T] = {
    new FutureSyncVar[T](result)
  }

  def setResult(v: T) {
    this.result.set(v)
  }
}