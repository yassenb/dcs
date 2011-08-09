package dcs.server

import swing._
import event.ButtonClicked

class ServerFrame(applicationState: ApplicationState = new ApplicationState)
    extends MainFrame with StateEventSubscriber {
  private val state = new Label

  applicationState.subscribe(this)
  
  {
    title = "dcs server"

    val remoteAddress = new TextField(30)
    val localAddress = new TextField(30)
    val addresses = applicationState.getAddresses
    remoteAddress.text = addresses._1
    localAddress.text = addresses._3
    val updateButton = new Button("update")

    contents = new BoxPanel(Orientation.Vertical) {
      contents.append(
        new Label("state"),
        state,
        new Label("remote address"),
        remoteAddress,
        new Label("local address to bind to"),
        localAddress,
        updateButton
      )
    }

    listenTo(updateButton)
    reactions += {
      case ButtonClicked(`updateButton`) =>
        applicationState.setAddresses(remoteAddress.text, localAddress.text)
    }
  }

  def submitToEventThread(block: => Unit) {
    java.awt.EventQueue.invokeLater(new Runnable {
      def run() {
        block
      }
    })
  }

  override def onError(error: Option[String]) {
    submitToEventThread({ state.text = error.getOrElse("OK") })
  }
}