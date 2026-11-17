package aktual.core.utils

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import java.net.InetAddress
import kotlin.test.Test

class InetAddressesTest {
  @Test
  fun `Private, link-local and CGNAT IPv4 are local`() {
    listOf(
        "10.0.0.1",
        "172.16.0.1",
        "172.31.255.255",
        "192.168.1.10",
        "169.254.1.1",
        "100.64.0.1",
        "100.127.255.255",
      )
      .forEach { assertThat(address(it).isLocalNetworkAddress(), it).isTrue() }
  }

  @Test
  fun `Public and loopback IPv4 aren't local`() {
    listOf("8.8.8.8", "172.15.0.1", "172.32.0.1", "192.169.0.1", "100.128.0.1", "127.0.0.1")
      .forEach { assertThat(address(it).isLocalNetworkAddress(), it).isFalse() }
  }

  @Test
  fun `IPv6 unique local, link-local, site-local and mapped private are local`() {
    listOf("fd12::1", "fc00::1", "fe80::1", "fec0::1", "::ffff:192.168.0.1").forEach {
      assertThat(address(it).isLocalNetworkAddress(), it).isTrue()
    }
  }

  @Test
  fun `Public and loopback IPv6 aren't local`() {
    listOf("2001::1", "::1", "::ffff:8.8.8.8").forEach {
      assertThat(address(it).isLocalNetworkAddress(), it).isFalse()
    }
  }

  // Literals don't need a DNS lookup
  private fun address(literal: String) = InetAddress.getByName(literal)
}
