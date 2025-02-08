package actual.api.model.account

import actual.api.client.ActualJson
import actual.login.model.LoginToken
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginResponseTest {
  @Test
  fun `Deserialize ok`() {
    val json = """
      {
        "status": "ok",
        "data": {
          "token": "abcd1234"
        }
      }
    """.trimIndent()

    assertEquals(
      actual = ActualJson.decodeFromString<LoginResponse>(json),
      expected = LoginResponse.Ok(
        data = LoginResponse.Data.Valid(token = LoginToken(value = "abcd1234")),
      ),
    )
  }

  @Test
  fun `Deserialize invalid`() {
    val json = """
      {
        "status": "ok",
        "data": {
          "token": null
        }
      }
    """.trimIndent()

    assertEquals(
      actual = ActualJson.decodeFromString<LoginResponse>(json),
      expected = LoginResponse.Ok(
        data = LoginResponse.Data.Invalid(),
      ),
    )
  }
}
