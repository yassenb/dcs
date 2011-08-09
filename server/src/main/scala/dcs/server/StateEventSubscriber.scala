package dcs.server

trait StateEventSubscriber {
  def onError(error: Option[String]) {}
}