package aktual.api.model.account

import aktual.core.model.Token
import aktual.test.AccountResponses
import aktual.test.testDecoding
import kotlin.test.Test

class BootstrapResponseTest {
  @Test
  fun `Decode from JSON 400`() = testDecoding(
    json = AccountResponses.BOOTSTRAP_ALREADY_400,
    expected = BootstrapResponse.Failure(
      reason = FailureReason.AlreadyBootstrapped,
    ),
  )

  @Test
  fun `Decode from JSON 200`() = testDecoding(
    json = AccountResponses.BOOTSTRAP_SUCCESS_200,
    expected = BootstrapResponse.Success(
      data = BootstrapResponse.Data(
        token = Token("92af386a-f727-431b-963a-f8cac5285878"),
      ),
    ),
  )
}
