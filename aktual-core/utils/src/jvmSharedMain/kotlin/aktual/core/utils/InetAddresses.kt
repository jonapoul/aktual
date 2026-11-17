package aktual.core.utils

import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

// Private, site-local or link-local. CGNAT is included since some LAN and VPN setups use it, and
// asking for the permission when it isn't needed costs less than leaving a connection to time out
fun InetAddress.isLocalNetworkAddress(): Boolean =
  isSiteLocalAddress || isLinkLocalAddress || isCgnat() || isUniqueLocal()

// 100.64.0.0/10
@Suppress("MagicNumber")
private fun InetAddress.isCgnat(): Boolean =
  this is Inet4Address && address[0].toInt() == 100 && address[1].toInt() and 0xC0 == 0x40

// fc00::/7. Java only knows the deprecated fec0::/10 as site-local
@Suppress("MagicNumber")
private fun InetAddress.isUniqueLocal(): Boolean =
  this is Inet6Address && address[0].toInt() and 0xFE == 0xFC
