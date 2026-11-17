package aktual.core.utils

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class LocalNetworkAddressTest {
  @Test
  fun `mDNS hosts are local`() {
    assertThat(isLocalNetworkHost("actual.local")).isTrue()
    assertThat(isLocalNetworkHost("Actual.LOCAL.")).isTrue()
    assertThat(isLocalNetworkHost("actual.localhost.com")).isFalse()
  }
}
