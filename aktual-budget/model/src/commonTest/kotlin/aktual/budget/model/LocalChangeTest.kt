package aktual.budget.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class LocalChangeTest {
  @Test
  fun `Enums use the SerialName value rather than the entry name`() {
    assertThat(localChange("d", "r", "c", RuleStage.Pre).value)
      .isEqualTo(MessageValue.String("pre"))
    assertThat(localChange("d", "r", "c", DateRangeType.Last3Months).value)
      .isEqualTo(MessageValue.String("Last 3 months"))
  }

  @Test
  fun `Null enum is a null value`() {
    val stage: RuleStage? = null
    assertThat(localChange("d", "r", "c", stage).value).isEqualTo(Null)
  }
}
