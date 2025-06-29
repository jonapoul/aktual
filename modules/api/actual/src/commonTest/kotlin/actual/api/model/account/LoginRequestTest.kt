package actual.api.model.account

import actual.account.model.LoginMethod
import actual.account.model.Password
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginRequestTest {
  val request = LoginRequest(
    loginMethod = LoginMethod.Password,
    password = Password("P@ssw0rd!"),
  )

  @Test
  fun `Convert to string with blocked out password`() {
    assertEquals(
      expected = "LoginRequest(loginMethod=Password, password=██)",
      actual = request.toString(),
    )
  }

  @Test
  fun `Serialize to json without blocking out password`() {
    assertEquals(
      expected = """{"loginMethod":"password","password":"P@ssw0rd!"}""",
      actual = Json.encodeToString(request),
    )
  }
}
