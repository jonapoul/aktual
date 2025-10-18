/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.api.model.account

import actual.core.model.LoginToken
import actual.test.AccountResponses
import actual.test.testDecoding
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
