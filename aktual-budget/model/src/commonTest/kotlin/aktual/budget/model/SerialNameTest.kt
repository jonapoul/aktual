package aktual.budget.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class SerialNameTest {
  @Test
  fun `Returns the SerialName value rather than the entry name`() {
    assertThat(RuleStage.Pre.serialName()).isEqualTo("pre")
    assertThat(DateRangeType.Last3Months.serialName()).isEqualTo("Last 3 months")
  }
}
