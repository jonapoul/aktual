package actual.api.model.account

import actual.account.model.LoginMethod.Password
import actual.test.AccountResponses
import actual.test.testDecoding
import kotlin.test.Test

class LoginMethodsResponseTest {
  @Test
  fun `Decode from JSON`() = testDecoding(
    json = AccountResponses.LOGIN_METHODS_SUCCESS_200,
    expected = LoginMethodsResponse.Success(
      methods = listOf(
        AvailableLoginMethod(
          method = Password,
          active = true,
          displayName = "Password",
        ),
      ),
    ),
  )
}
