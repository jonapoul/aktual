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
package aktual.api.model.account

import aktual.core.model.LoginMethod.Password
import aktual.test.AccountResponses
import aktual.test.testDecoding
import kotlin.test.Test

class NeedsBoostrapResponseTest {
  @Test
  fun `Decode from JSON`() = testDecoding(
    json = AccountResponses.NEEDS_BOOTSTRAP_SUCCESS_200,
    expected = NeedsBootstrapResponse.Success(
      data = NeedsBootstrapResponse.Data(
        bootstrapped = true,
        loginMethod = Password,
        availableLoginMethods = listOf(
          AvailableLoginMethod(
            method = Password,
            active = true,
            displayName = "Password",
          ),
        ),
        multiuser = false,
      ),
    ),
  )
}
