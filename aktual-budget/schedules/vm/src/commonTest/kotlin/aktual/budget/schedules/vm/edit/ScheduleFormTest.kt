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

  @Test
  fun `Blank input is zero`() {
    assertThat(parseAmountInput("")).isEqualTo(Amount(0L))
    assertThat(parseAmountInput("  ")).isEqualTo(Amount(0L))
  }

  @Test
  fun `Input reads the last separator as the decimal point`() {
    assertThat(parseAmountInput("1,200.50")).isEqualTo(Amount(120_050L))
    assertThat(parseAmountInput("1.200,50")).isEqualTo(Amount(120_050L))
  }
}
