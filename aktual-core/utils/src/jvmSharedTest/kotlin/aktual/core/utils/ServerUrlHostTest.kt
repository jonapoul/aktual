package aktual.core.utils

import aktual.core.model.ServerUrl
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class ServerUrlHostTest {
  @Test
  fun `Host strips port, path and user info`() {
    assertThat(ServerUrl(Https, "budget.example.com").host).isEqualTo("budget.example.com")
    assertThat(ServerUrl(Http, "192.168.1.5:5006/actual").host).isEqualTo("192.168.1.5")
    assertThat(ServerUrl(Https, "user@host.lan:443").host).isEqualTo("host.lan")
  }

  @Test
  fun `Host strips query, fragment and whitespace`() {
    assertThat(ServerUrl(Http, "nas?x=1").host).isEqualTo("nas")
    assertThat(ServerUrl(Http, "nas#a").host).isEqualTo("nas")
    assertThat(ServerUrl(Http, " nas.local:5006 ").host).isEqualTo("nas.local")
  }

  @Test
  fun `Host strips IPv6 brackets`() {
    assertThat(ServerUrl(Http, "[fe80::1]:5006/path").host).isEqualTo("fe80::1")
    assertThat(ServerUrl(Http, "[fd00::5]").host).isEqualTo("fd00::5")
  }

  @Test
  fun `Host keeps names URI won't parse as hostnames`() {
    assertThat(ServerUrl(Http, "my_nas:5006").host).isEqualTo("my_nas")
  }

  @Test
  fun `Host is null for unparseable URLs`() {
    assertThat(ServerUrl(Http, "bad host").host).isNull()
  }
}
