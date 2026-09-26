package aktual.budget.reports.vm

import aktual.budget.model.Amount
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month.APRIL
import kotlinx.datetime.Month.FEBRUARY
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.Month.MARCH
import kotlinx.datetime.Month.MAY
import kotlinx.datetime.YearMonth

class CrossoverTest {
  @Test
  fun `Crosses over in the first projected month when income already covers expenses`() {
    val data =
      calculate(
        months = listOf(JAN, FEB, MAR),
        expenses = listOf(1000, 1000, 1000),
        balances = listOf(300_000, 300_000, 300_000),
      )

    assertThat(data.crossover).isEqualTo(YearMonth(2024, APRIL))
    assertThat(data.items.size).isEqualTo(4)
    assertThat(data.items[MAR])
      .isNotNull()
      .prop(CrossoverDatum::investmentIncome)
      .isEqualTo(Amount(1000))
    // Jan 15 to Apr 1 is two whole months
    assertThat(data.yearsToRetire).isEqualTo(2.0 / 12)
  }

  @Test
  fun `Contributions grow the balance until it crosses over`() {
    val data =
      calculate(
        months = listOf(JAN, FEB),
        expenses = listOf(1000, 1000),
        balances = listOf(0, 0),
        params = PARAMS.copy(expectedContribution = 100_000),
      )

    assertThat(data.crossover).isEqualTo(YearMonth(2024, MAY))
    assertThat(data.items[YearMonth(2024, MAY)])
      .isEqualTo(
        CrossoverDatum(
          investmentIncome = Amount(1000),
          expenses = Amount(1000),
          nestEgg = Amount(300_000),
          adjustedExpenses = Amount(1000),
        )
      )
  }

  @Test
  fun `Adjustment factor scales the target income`() {
    val data =
      calculate(
        months = listOf(JAN),
        expenses = listOf(1000),
        balances = listOf(0),
        params = PARAMS.copy(expectedContribution = 300_000, expenseAdjustmentFactor = 2.0),
      )

    assertThat(data.crossover).isEqualTo(YearMonth(2024, MARCH))
    assertThat(data.items[YearMonth(2024, MARCH)])
      .isNotNull()
      .prop(CrossoverDatum::adjustedExpenses)
      .isEqualTo(Amount(2000))
  }

  @Test
  fun `No crossover within fifty years`() {
    val data = calculate(months = listOf(JAN), expenses = listOf(1000), balances = listOf(0))

    assertThat(data.crossover).isNull()
    assertThat(data.yearsToRetire).isNull()
    assertThat(data.items.size).isEqualTo(601)
  }

  @Test
  fun `Historical growth is used when there's no estimated return`() {
    val data =
      calculate(
        months = listOf(JAN, FEB),
        expenses = listOf(0, 0),
        balances = listOf(100_000, 121_000),
        params = PARAMS.copy(estimatedReturn = null),
      )

    assertThat(data.items[MAR]).isNotNull().prop(CrossoverDatum::nestEgg).isEqualTo(Amount(146_410))
  }

  @Test
  fun `Nothing to show without months`() {
    val data = calculate(months = emptyList(), expenses = emptyList(), balances = emptyList())

    assertThat(data.items).isEmpty()
    assertThat(data.crossover).isNull()
  }

  @Test
  fun `Median of an even count averages the middle two`() {
    assertThat(median(listOf(4.0, 1.0, 3.0, 2.0))).isEqualTo(2.5)
    assertThat(median(emptyList())).isEqualTo(0.0)
  }

  @Test
  fun `Hampel filter drops outliers`() {
    assertThat(hampelFilteredMedian(listOf(10.0, 12.0, 11.0, 13.0, 500.0))).isEqualTo(11.5)
    assertThat(hampelFilteredMedian(listOf(100.0, 100.0, 100.0, 10_000.0))).isEqualTo(100.0)
  }

  private fun calculate(
    months: List<YearMonth>,
    expenses: List<Long>,
    balances: List<Long>,
    params: CrossoverParams = PARAMS,
  ) =
    calculateCrossover(
      title = null,
      months = months,
      expenses = expenses,
      balances = balances,
      params = params,
      today = LocalDate(2024, JANUARY, 15),
    )

  private companion object {
    val JAN = YearMonth(2024, JANUARY)
    val FEB = YearMonth(2024, FEBRUARY)
    val MAR = YearMonth(2024, MARCH)

    val PARAMS =
      CrossoverParams(
        safeWithdrawalRate = 0.04,
        estimatedReturn = 0.0,
        expectedContribution = null,
        projectionType = Hampel,
        expenseAdjustmentFactor = 1.0,
      )
  }
}
