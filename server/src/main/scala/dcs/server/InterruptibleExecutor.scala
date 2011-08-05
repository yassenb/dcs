package dcs.server

import java.util.concurrent.{Executors, ExecutorService}

class InterruptibleExecutor {
  private[this] var singleThreadExecutor:ExecutorService = _

  def submit(f: () => Unit) {
    if (singleThreadExecutor != null) {
      singleThreadExecutor.shutdownNow()
    }

    singleThreadExecutor = Executors.newSingleThreadExecutor()
    singleThreadExecutor.submit(new Runnable {
      def run() {
        f()
      }
    })
  }
}