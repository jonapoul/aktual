package actual.api.model.account

import actual.account.model.LoginMethod
import actual.account.model.Password
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginRequestTest {
  @Test
  fun secretPassword() {
    val password = Password("P@ssw0rd!")
    val request = LoginRequest(LoginMethod.Password, password)
    assertEquals(
      expected = "LoginRequest(loginMethod=Password, password=■■■)",
      actual = request.toString(),
    )
  }
}
