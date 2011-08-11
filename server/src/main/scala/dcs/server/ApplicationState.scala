package dcs.server

import concurrent.Lock
import dcs.common.LockContext

class ApplicationState(createConfiguration: () => Configuration = () => new Configuration) {
  private val addressesLock = new Lock
  private var eventSubscriber: StateEventSubscriber = new Object with StateEventSubscriber
  private var (remoteAddress, port, localAddress) = createConfiguration().getAddresses
  private var error: Option[String] = None

  def subscribe(subscriber: StateEventSubscriber) {
    eventSubscriber = subscriber
  }
  
  /**
   * Retrieve the remote address and port to contact and the local address to bind to
   *
   * @return {@code (remoteAddress, remotePort, localAddress)}. If {@code localAddress} is {@code null} an arbitrary
   * address should be chosen
   */
  def getAddresses: (String, Int, String) = {
    LockContext(addressesLock) {
      (remoteAddress, port, localAddress)
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