package dcs.client

import java.io.Serializable
import dcs.common.Task
import java.util.concurrent.Future
import concurrent.SyncVar

class DistributedComputeService private {
  def submit[T <: Serializable](task: Task[T]): Future[T] = {
    val v = new SyncVar[T]
    v.set(task.execute())
    new FutureSyncVar[T](v)
  }
}

object DistributedComputeService {
  private[this] val dcs = new DistributedComputeService

  def submit[T <: Serializable](task: Task[T]): Future[T] = {
    dcs.submit(task)
  }
}