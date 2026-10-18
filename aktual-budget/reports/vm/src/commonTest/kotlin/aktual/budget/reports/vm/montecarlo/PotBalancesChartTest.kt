package aktual.budget.reports.vm.montecarlo

import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.McPot
import aktual.budget.reports.vm.McRunDetailRow
import aktual.budget.reports.vm.surplusPot
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

class PotBalancesChartTest {
  @Test
  fun `Every pot gets a series in pot order, coloured like the cashflow chart`() {
    val chart = buildPotBalancesChart(ROWS, CONFIG, paletteSize = 2)
    val cashflow = buildCashflowChart(ROWS, CONFIG, paletteSize = 2)

    assertThat(chart.series)
      .containsExactly(
        CashflowSeries(Pot, index = 0, name = "", colorIndex = 0),
        CashflowSeries(Pot, index = 1, name = "Pension", colorIndex = 1),
        CashflowSeries(Pot, index = 2, name = "", colorIndex = 0),
      )
    assertThat(chart.series.map { it.colorIndex })
      .isEqualTo(cashflow.inflows.filter { it.kind == Pot }.map { it.colorIndex })
  }

  @Test
  fun `Each year holds every pot's end balance and their total`() {
    val chart = buildPotBalancesChart(ROWS, CONFIG, paletteSize = 9)

    assertThat(chart.years[0]).all {
      prop(PotBalancesYear::year).isEqualTo(1)
      prop(PotBalancesYear::age).isEqualTo(60)
      prop(PotBalancesYear::afterDepletion).isFalse()
      prop(PotBalancesYear::balances).containsExactly(100L, 0L, 250L)
      prop(PotBalancesYear::total).isEqualTo(350L)
    }
    // Years after a failure carry zero balances
    assertThat(chart.years[1]).all {
      prop(PotBalancesYear::age).isEqualTo(61)
      prop(PotBalancesYear::afterDepletion).isTrue()
      prop(PotBalancesYear::balances).containsExactly(0L, 0L, 0L)
    }
  }

  private companion object {
    val CONFIG =
      McConfig(
        pots =
          persistentListOf(surplusPot("s"), McPot(id = "a", name = "Pension"), McPot(id = "b")),
        currentAge = 60,
      )

    val ROWS =
      listOf(
        row(year = 1, potBalances = persistentListOf(100, 0, 250)),
        row(year = 2, potBalances = persistentListOf()).copy(afterDepletion = true),
      )

    fun row(year: Int, potBalances: ImmutableList<Long>) =
      McRunDetailRow(
        year = year,
        startBalance = 0,
        withdrawal = 0,
        plannedSpending = 0,
        spent = 0,
        growth = 0,
        endBalance = potBalances.sum(),
        potBalances = potBalances,
        potStartBalances = persistentListOf(0, 0, 0),
        inflation = null,
        income = 0,
        incomeAmounts = persistentListOf(),
        incomeTax = 0,
        unspentIncome = 0,
        surplusSaved = 0,
        contributions = 0,
        potContributions = persistentListOf(0, 0, 0),
        contributionAmounts = persistentListOf(),
        potWithdrawals = persistentListOf(0, 0, 0),
        potTaxes = persistentListOf(0, 0, 0),
        potTaxables = persistentListOf(0, 0, 0),
        taxPaid = 0,
        feesPaid = 0,
        potFees = persistentListOf(0, 0, 0),
        potReturns = persistentListOf(null, null, null),
      )
  }
}
