package dcs.server

import concurrent.Lock
import dcs.common.LockContext
import java.util.UUID

class ApplicationState(configuration: Configuration = new Configuration) {
  private val addressesLock = new Lock
  private var eventSubscriber: StateEventSubscriber = new Object with StateEventSubscriber
  private var (remoteAddress, port, localAddress) = {
    val x = configuration.getAddresses
    (x.remoteAddress, x.port, x.localAddress)
  }
  private var error: Option[String] = None
  val serverID: UUID = UUID.randomUUID()

  def subscribe(subscriber: StateEventSubscriber) {
    eventSubscriber = subscriber
  }

  def getAddresses: Addresses = {
    LockContext(addressesLock) {
      Addresses(remoteAddress, port, localAddress)
    }
  }

  def setAddresses(remoteAddress: String, localAddress: String) {
    LockContext(addressesLock) {
      this.remoteAddress = remoteAddress
      this.localAddress = localAddress
    }
  }
  
  def setError(error: Option[String]) {
    this.error = error
    eventSubscriber.onError(error)
  }

  def save() {
    Configuration.save(remoteAddress, localAddress)
  }
}