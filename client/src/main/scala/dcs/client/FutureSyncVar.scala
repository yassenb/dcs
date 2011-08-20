package dcs.client

import concurrent.SyncVar
import java.util.concurrent.{TimeoutException, TimeUnit, Future}

class FutureSyncVar[T](v: SyncVar[T]) extends Future[T] {
  /**
   * Has no effect, this future is not cancellable
   *
   * @return <tt>false</tt>
   */
  def cancel(mayInterruptIfRunning: Boolean): Boolean = false

  /**
   * Always returns false because this future is not cancellable
   * 
   * @return <tt>false</tt>
   */
  def isCancelled: Boolean = false

  /** {@inheritDoc} */
  def isDone: Boolean = v.isSet

  /** {@inheritDoc} */
  def get(): T = v.get

  /** {@inheritDoc} */
  def get(timeout: Long, unit: TimeUnit): T = {
    val res = v.get(unit.toMillis(timeout))
    if (res.isDefined) {
      res.get
    } else {
      throw new TimeoutException
    }
  }
}