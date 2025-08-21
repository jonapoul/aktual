package actual.api.model.account

import actual.core.model.LoginToken
import actual.test.AccountResponses
import actual.test.testDecoding
import kotlin.test.Test

class LoginResponseTest {
  @Test
  fun `Decode from JSON`() = testDecoding(
    json = AccountResponses.LOGIN_SUCCESS_200,
    expected = LoginResponse.Success(
      data = LoginResponse.Data.Valid(
        token = LoginToken("0f171d2d-c82c-496c-bd20-30eae2abdcf1"),
      ),
    ),
  )
}
