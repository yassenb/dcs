package dcs.server

import dcs.common.Constants

class Configuration {
  /**
   * Retrieve the remote address and port to contact and the local address to bind to
   *
   * @return {@code (remoteAddress, remotePort, localAddress)}. If {@code localAddress} is {@code null} an arbitrary
   * address should be chosen
   */
  def getAddresses: (String, Int, String) = {
    ("localhost", Constants.PORT, null)
  }
}