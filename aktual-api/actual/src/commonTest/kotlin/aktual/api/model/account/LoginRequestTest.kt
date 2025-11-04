/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.api.model.account

import aktual.core.model.Password
import aktual.test.testEncoding
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
