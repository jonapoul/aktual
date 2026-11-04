package aktual.budget.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class SortOrderTest {
  @Test
  fun `First row gets one increment`() {
    assertThat(sortOrderAfter(null)).isEqualTo(SORT_INCREMENT)
  }

  @Test
  fun `Next row goes one increment after the max`() {
    assertThat(sortOrderAfter(2.0 * SORT_INCREMENT)).isEqualTo(3 * SORT_INCREMENT)
  }

  @Test
  fun `Fractional max is rounded down`() {
    assertThat(sortOrderAfter(SORT_INCREMENT + 0.5)).isEqualTo(2 * SORT_INCREMENT)
  }
}
