package aktual.budget.reports.vm.montecarlo

import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.McContribution
import aktual.budget.reports.vm.McIncomeStream
import aktual.budget.reports.vm.McPot
import aktual.budget.reports.vm.McRunDetailRow
import aktual.budget.reports.vm.McSpendingPhase
import aktual.budget.reports.vm.surplusPot
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf

class CashflowChartTest {
  @Test
  fun `Series cover every pot, and only the streams and contributions that move money`() {
    val chart = buildCashflowChart(ROWS, CONFIG, paletteSize = 9)

    assertThat(chart).all {
      transform { it.inflows.map(CashflowSeries::kind) }
        .containsExactly(CashflowSeriesKind.Pot, CashflowSeriesKind.Pot, CashflowSeriesKind.Income)
      transform { it.inflows.last() }
        .isEqualTo(CashflowSeries(Income, index = 0, name = "Job", colorIndex = 2))
      transform { it.outflows.map(CashflowSeries::kind) }
        .containsExactly(
          CashflowSeriesKind.Phase,
          CashflowSeriesKind.Phase,
          CashflowSeriesKind.Tax,
          CashflowSeriesKind.Contribution,
          CashflowSeriesKind.Surplus,
        )
      transform { it.groups.map(CashflowGroup::kind) }
        .containsExactly(
          CashflowGroupKind.Withdrawals,
          CashflowGroupKind.Income,
          CashflowGroupKind.Tax,
          CashflowGroupKind.Spending,
          CashflowGroupKind.Contributions,
          CashflowGroupKind.Saved,
        )
    }
  }

  @Test
  fun `Spending is charged to the phase active that year, and outflows are negative`() {
    val chart = buildCashflowChart(ROWS, CONFIG, paletteSize = 9)

    // Pots, job income, phase 1, phase 2, tax, contribution, surplus
    assertThat(chart.years[0]).all {
      prop(CashflowYear::age).isEqualTo(60)
      prop(CashflowYear::amounts)
        .containsExactly(0L, 1_000L, 5_000L, -4_000L, 0L, -300L, -500L, -200L)
      prop(CashflowYear::unspentIncome).isEqualTo(0L)
    }
    assertThat(chart.years[1]).all {
      prop(CashflowYear::age).isEqualTo(65)
      prop(CashflowYear::afterDepletion).isEqualTo(true)
      prop(CashflowYear::amounts).containsExactly(0L, 0L, 0L, 0L, -3_000L, 0L, 0L, 0L)
    }
  }

  private companion object {
    val CONFIG =
      McConfig(
        pots = persistentListOf(surplusPot("s"), McPot(id = "a")),
        incomeStreams =
          persistentListOf(McIncomeStream(id = "job", name = "Job"), McIncomeStream(id = "db")),
        contributions = persistentListOf(McContribution(id = "c", potId = "a")),
        spendingPhases =
          persistentListOf(
            McSpendingPhase(id = "p2", fromAge = 65),
            McSpendingPhase(id = "p1"),
          ),
        currentAge = 60,
      )

    val ROWS =
      [
        row(year = 1)
          .copy(
            potWithdrawals = persistentListOf(0, 1_000),
            incomeAmounts = persistentListOf(5_000, 0),
            plannedSpending = 4_000,
            taxPaid = 100,
            incomeTax = 200,
            contributionAmounts = persistentListOf(500),
            surplusSaved = 200,
            unspentIncome = 200,
          ),
        row(year = 6).copy(plannedSpending = 3_000, afterDepletion = true),
      ]

    fun row(year: Int) =
      McRunDetailRow(
        year = year,
        startBalance = 0,
        withdrawal = 0,
        plannedSpending = 0,
        spent = 0,
        growth = 0,
        endBalance = 0,
        potBalances = persistentListOf(0, 0),
        potStartBalances = persistentListOf(0, 0),
        inflation = null,
        income = 0,
        incomeAmounts = persistentListOf(0, 0),
        incomeTax = 0,
        unspentIncome = 0,
        surplusSaved = 0,
        contributions = 0,
        potContributions = persistentListOf(0, 0),
        contributionAmounts = persistentListOf(0),
        potWithdrawals = persistentListOf(0, 0),
        potTaxes = persistentListOf(0, 0),
        potTaxables = persistentListOf(0, 0),
        taxPaid = 0,
        feesPaid = 0,
        potFees = persistentListOf(0, 0),
        potReturns = persistentListOf(null, null),
      )
  }
}
