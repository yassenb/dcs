package dcs.server

import dcs.common.LockContext
import concurrent.Lock

class ApplicationState(createConfiguration: () => Configuration = () => new Configuration) {
  private val addressesLock = new Lock
  private var (remoteAddress, port, localAddress) = createConfiguration().getAddresses
  
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
}