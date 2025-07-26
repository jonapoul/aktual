package actual.api.model.account

import actual.account.model.Password
import actual.test.testEncoding
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginRequestTest {
  private val password = Password("P@ssw0rd!")

  @Test
  fun `Convert to string with blocked out password`() {
    assertEquals(
      expected = "Password(password=██, loginMethod=Password)",
      actual = LoginRequest.Password(password).toString(),
    )
  }

  @Test
  fun `Serialize to json without blocking out password`() = testEncoding(
    model = LoginRequest.Password(password),
    expected = """
      {
        "password": "P@ssw0rd!",
        "loginMethod": "password"
      }
    """.trimIndent(),
  )

  @Test
  fun `Encode header request`() = testEncoding(
    model = LoginRequest.Header(),
    expected = """
      {
        "loginMethod": "header"
      }
    """.trimIndent(),
  )

  @Test
  fun `Encode openID request`() = testEncoding(
    model = LoginRequest.OpenId(password, returnUrl = "https://url.com"),
    expected = """
      {
        "password": "P@ssw0rd!",
        "returnUrl": "https://url.com",
        "loginMethod": "openid"
      }
    """.trimIndent(),
  )
}
