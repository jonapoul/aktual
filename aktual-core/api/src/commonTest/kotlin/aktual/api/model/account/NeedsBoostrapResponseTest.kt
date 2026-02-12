package aktual.api.model.account

import aktual.core.model.LoginMethod.Password
import aktual.test.AccountResponses
import aktual.test.testDecoding
import kotlin.test.Test

class NeedsBoostrapResponseTest {
  @Test
  fun `Decode from JSON`() =
    testDecoding(
      json = AccountResponses.NEEDS_BOOTSTRAP_SUCCESS_200,
      expected =
        NeedsBootstrapResponse.Success(
          data =
            NeedsBootstrapResponse.Data(
              bootstrapped = true,
              loginMethod = Password,
              availableLoginMethods =
                listOf(
                  AvailableLoginMethod(method = Password, active = true, displayName = "Password")
                ),
              multiuser = false,
            )
        ),
    )
}
