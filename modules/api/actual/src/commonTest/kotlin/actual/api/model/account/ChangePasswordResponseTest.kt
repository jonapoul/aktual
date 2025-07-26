package actual.api.model.account

import actual.test.AccountResponses
import actual.test.testDecoding
import kotlin.test.Test

class ChangePasswordResponseTest {
  @Test
  fun `Decode from JSON 200`() = testDecoding(
    json = AccountResponses.CHANGE_PASSWORD_SUCCESS_200,
    expected = ChangePasswordResponse.Success(),
  )

  @Test
  fun `Decode from JSON 401`() = testDecoding(
    json = AccountResponses.CHANGE_PASSWORD_TOKEN_NOT_FOUND_401,
    expected = ChangePasswordResponse.Failure(
      reason = FailureReason.Unauthorized,
      details = "token-not-found",
    ),
  )
}
