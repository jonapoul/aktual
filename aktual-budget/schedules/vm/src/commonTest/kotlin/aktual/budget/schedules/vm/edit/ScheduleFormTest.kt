package aktual.budget.schedules.vm.edit

import aktual.budget.model.Amount
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class ScheduleFormTest {
  @Test
  fun `Range stays ascending when flipped to a payment`() {
    val range = ScheduleAmount.Between(Amount(1_000L), Amount(5_000L))
    assertThat(range.withDeposit(false))
      .isEqualTo(ScheduleAmount.Between(Amount(-5_000L), Amount(-1_000L)))
  }

  @Test
  fun `Range stays ascending when flipped to a deposit`() {
    val range = ScheduleAmount.Between(Amount(-1_000L), Amount(-5_000L))
    assertThat(range.withDeposit(true))
      .isEqualTo(ScheduleAmount.Between(Amount(1_000L), Amount(5_000L)))
  }
}
