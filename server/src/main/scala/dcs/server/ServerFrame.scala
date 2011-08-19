package dcs.server

import swing._
import Swing._
import event.ButtonClicked

class ServerFrame(applicationState: ApplicationState = new ApplicationState)
    extends MainFrame with StateEventSubscriber {
  // initialize the label with some text otherwise for some reason it doesn't take up space when doing the layout
  private val state = new Label(" ")

  applicationState.subscribe(this)
  
  {
    title = "dcs server"

    val remoteAddress = new TextField(30)
    val localAddress = new TextField(30) {
      // using HTML in order to have line breaks
      tooltip = "<html>This is the locally configured network address that the program <br/> should bind to when" +
        "connecting to the remote address. <br/> Leave blank to automatically pick one.</html>"
    }
    val addresses = applicationState.getAddresses
    remoteAddress.text = addresses.remoteAddress
    localAddress.text = addresses.localAddress
    val updateButton = new Button("update")

    contents = new GridBagPanel {
      border = EmptyBorder(5)
      val constraints = new Constraints {
        fill = GridBagPanel.Fill.Horizontal
        gridwidth = 0
      }
      def etchedWithBorder = {
        CompoundBorder(EtchedBorder, EmptyBorder(0, 4, 2, 3))
      }
      val components = List(
        new BoxPanel(Orientation.Vertical) {
          border = TitledBorder(etchedWithBorder, "connection configuration")
          contents.append(
            new Label("remote address"),
            remoteAddress,
            RigidBox((0, 7)),
            new Label("bind address"),
            localAddress,
            RigidBox((0, 5)),
            new BoxPanel(Orientation.Horizontal) {
              contents.append(
                HGlue,
                updateButton
              )
            }
          )
          contents.foreach({_.xLayoutAlignment = java.awt.Component.LEFT_ALIGNMENT})
        },
        RigidBox((0, 5)),
        new BoxPanel(Orientation.Vertical) {
          border = TitledBorder(etchedWithBorder, "state")
          contents.append(
            state
          )
        }
      )
      for (component <- components) {
        add(component, constraints)
      }
    }

    listenTo(updateButton)
    reactions += {
      case ButtonClicked(`updateButton`) =>
        applicationState.setAddresses(remoteAddress.text, localAddress.text)
    }
  }

  override def onError(error: Option[String]) {
    onEDT({ state.text = error.getOrElse("OK") })
  }

  override def closeOperation() {
    applicationState.save()
    super.closeOperation()
  }
}