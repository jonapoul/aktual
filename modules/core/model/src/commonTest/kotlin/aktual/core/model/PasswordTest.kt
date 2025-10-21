/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class PasswordTest {
  @Test
  fun `Value redacted from toString`() {
    val password = Password("P@ssw0rd!")
    assertThat(password.toString()).isEqualTo("Password(██)")
  }
}
