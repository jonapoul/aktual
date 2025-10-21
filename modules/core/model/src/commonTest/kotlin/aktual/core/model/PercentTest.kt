/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class PercentTest {
  @Test
  fun `To string with decimals`() {
    assertThat(12.344.percent.toString(decimalPlaces = 2)).isEqualTo("12.34%")
    assertThat(12.346.percent.toString(decimalPlaces = 2)).isEqualTo("12.35%")
    assertThat(123.4.percent.toString(decimalPlaces = 2)).isEqualTo("123.40%")
    assertThat(123.4.percent.toString()).isEqualTo("123%")
  }
}
