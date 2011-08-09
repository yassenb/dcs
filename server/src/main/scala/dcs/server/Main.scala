package dcs.server

import scala.concurrent.ops._
import swing.SimpleSwingApplication
import javax.swing.UIManager
import dcs.common.{TaskResponseProtocol, TaskRequestProtocol, PingProtocol}

object Main extends SimpleSwingApplication {
  try {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
  } catch {
    case _ =>
  }

  def top = {
    val state = new ApplicationState

    val cp = new ClientPoller(new PingProtocol(_, _),
                              new TaskRequestProtocol(_, _),
                              new TaskResponseProtocol(_, _),
                              TaskExecutor.execute(_, new NetworkClassLoader(() => state.getAddresses)),
                              new InterruptibleExecutor,
                              state)
    spawn {
      cp.startPolling()
    }

    new ServerFrame(state)
  }
}
