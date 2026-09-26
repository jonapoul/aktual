package aktual.budget.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class ResolvedDateRangeTest {
  @Test
  fun `Unknown range resolves the same as all time`() {
    val today = LocalDate(2026, 9, 26)

    assertThat(DateRangeType.Unknown.resolve(today)).isEqualTo(DateRangeType.AllTime.resolve(today))
  }
}
