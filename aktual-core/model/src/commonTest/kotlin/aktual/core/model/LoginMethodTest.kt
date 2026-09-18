package aktual.core.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.serialization.json.Json

class LoginMethodTest {
  @Test
  fun `Decode known values`() {
    assertThat(decode("password")).isEqualTo(LoginMethod.Password)
    assertThat(decode("header")).isEqualTo(Header)
    assertThat(decode("openid")).isEqualTo(OpenId)
  }

  @Test
  fun `Decode unknown value as fallback`() {
    assertThat(decode("saml")).isEqualTo(Unknown)
  }

  @Test
  fun `Encode by serial name`() {
    assertThat(Json.encodeToString<LoginMethod>(OpenId)).isEqualTo("\"openid\"")
  }

  private fun decode(value: String) = Json.decodeFromString<LoginMethod>("\"$value\"")
}
