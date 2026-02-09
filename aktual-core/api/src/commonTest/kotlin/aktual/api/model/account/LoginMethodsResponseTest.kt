package aktual.api.model.account

import aktual.core.model.LoginMethod.Password
import aktual.test.AccountResponses
import aktual.test.testDecoding
import kotlin.test.Test

class LoginMethodsResponseTest {
  @Test
  fun `Decode from JSON`() =
      testDecoding(
          json = AccountResponses.LOGIN_METHODS_SUCCESS_200,
          expected =
              LoginMethodsResponse.Success(
                  methods =
                      listOf(
                          AvailableLoginMethod(
                              method = Password,
                              active = true,
                              displayName = "Password",
                          ),
                      ),
              ),
      )
}
