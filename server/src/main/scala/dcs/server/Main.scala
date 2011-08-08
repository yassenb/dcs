package dcs.server

import scala.concurrent.ops._
import swing.SimpleSwingApplication
import javax.swing.UIManager

object Main extends SimpleSwingApplication {
  try {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
  } catch {
    case _ =>
  }

  def top = {
    val state = new ApplicationState

    val cp = new ClientPoller(applicationState = state)
    spawn {
      cp.startPolling()
    }

    new ServerFrame(state)
  }
}
