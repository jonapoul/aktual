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

import actual.core.model.LoginMethod.Password
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
