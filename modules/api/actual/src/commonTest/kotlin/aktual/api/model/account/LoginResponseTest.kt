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

import aktual.core.model.LoginToken
import aktual.test.AccountResponses
import aktual.test.testDecoding
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
