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

import actual.core.model.Password
import actual.test.testEncoding
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class LoginRequestTest {
  private val password = Password("P@ssw0rd!")

  @Test
  fun `Convert to string with blocked out password`() {
    assertThat(LoginRequest.Password(password).toString())
      .isEqualTo("Password(password=██, loginMethod=Password)")
  }

  @Test
  fun `Serialize to json without blocking out password`() = testEncoding(
    model = LoginRequest.Password(password),
    expected = """
      {
        "password": "P@ssw0rd!",
        "loginMethod": "password"
      }
    """.trimIndent(),
  )

  @Test
  fun `Encode header request`() = testEncoding(
    model = LoginRequest.Header(),
    expected = """
      {
        "loginMethod": "header"
      }
    """.trimIndent(),
  )

  @Test
  fun `Encode openID request`() = testEncoding(
    model = LoginRequest.OpenId(password, returnUrl = "https://url.com"),
    expected = """
      {
        "password": "P@ssw0rd!",
        "returnUrl": "https://url.com",
        "loginMethod": "openid"
      }
    """.trimIndent(),
  )
}
