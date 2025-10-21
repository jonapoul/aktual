/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.api.model.account

import aktual.core.model.LoginToken
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
        token = LoginToken("92af386a-f727-431b-963a-f8cac5285878"),
      ),
    ),
  )
}
