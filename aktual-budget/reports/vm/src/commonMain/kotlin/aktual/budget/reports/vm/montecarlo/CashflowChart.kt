package aktual.budget.reports.vm.montecarlo

import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.McRunDetailRow
import aktual.budget.reports.vm.activeAt
import aktual.budget.reports.vm.resolve
import aktual.core.model.unaryPlus
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

// Ported from packages/desktop-client/src/components/reports/graphs/util/monteCarloCashflowChart.ts

enum class CashflowSeriesKind {
  Pot,
  Income,
  Phase,
  Tax,
  Contribution,
  Surplus,
}

// One stacked bar series. Labels are left to the UI: a user-given name, else a numbered fallback
data class CashflowSeries(
  val kind: CashflowSeriesKind,
  // The pot, income stream, phase or contribution it belongs to, in configured order
  val index: Int,
  val name: String,
  // Position in the chart's qualitative palette
  val colorIndex: Int,
)

@Immutable
data class CashflowYear(
  val year: Int,
  val age: Int,
  // A synthetic year after the plan ran out: spending with no funding
  val afterDepletion: Boolean,
  // Income the year didn't need, which left the plan
  val unspentIncome: Long,
  // One per series, inflows then outflows. Positive for money in, negative for money out
  val amounts: ImmutableList<Long>,
)

enum class CashflowGroupKind {
  Withdrawals,
  Income,
  Tax,
  Spending,
  Contributions,
  Saved,
}

// A headed section of the tooltip. Single-series groups (tax, saved) show only the heading row
data class CashflowGroup(
  val kind: CashflowGroupKind,
  // Indices into the chart's combined series list
  val series: ImmutableList<Int>,
  val listMembers: Boolean,
)

@Immutable
data class CashflowChart(
  // Stacked above zero: pot withdrawals, then income streams
  val inflows: ImmutableList<CashflowSeries>,
  // Stacked below zero: planned spending by phase, tax, contributions, then surplus savings
  val outflows: ImmutableList<CashflowSeries>,
  val years: ImmutableList<CashflowYear>,
  val groups: ImmutableList<CashflowGroup>,
) {
  val series: List<CashflowSeries>
    get() = inflows + outflows
}

// buildMonteCarloCashflowChart(): spending shows the plan rather than what was delivered, so in a
// shortfall year the inflows visibly fall short of it. Fees stay out, since they never pass through
// the user's hands
fun buildCashflowChart(
  rows: List<McRunDetailRow>,
  config: McConfig,
  paletteSize: Int,
): CashflowChart {
  val pots = config.pots
  val phases = config.spendingPhases.resolve()
  val hasTax = rows.any { it.taxPaid != 0L || it.incomeTax != 0L }

  // Inflows take colours from the start of the palette and outflows from the end, so the two sides
  // only share a colour once the palette is exhausted
  fun inflowColor(index: Int) = index % paletteSize

  fun outflowColor(index: Int) = paletteSize - 1 - index % paletteSize

  val potSeries = pots.mapIndexed { i, pot -> CashflowSeries(Pot, i, pot.name, inflowColor(i)) }
  // Only streams that pay something in this run get a series
  val incomeSeries =
    config.incomeStreams.mapIndexedNotNull { i, stream ->
      if (rows.any { it.incomeAmounts.getOrElse(i) { 0 } != 0L }) {
        CashflowSeries(Income, i, stream.name, inflowColor(pots.size + i))
      } else {
        null
      }
    }
  val phaseSeries = phases.mapIndexed { i, phase ->
    // Numbered as the spending editor lists them; the default stand-in phase reads as Phase 1
    val configuredIndex = config.spendingPhases.indexOf(phase).coerceAtLeast(0)
    CashflowSeries(Phase, configuredIndex, phase.name, outflowColor(i))
  }
  val taxSeries = CashflowSeries(Tax, 0, "", -1)
  val contributionSeries =
    config.contributions.mapIndexedNotNull { i, contribution ->
      if (rows.any { it.contributionAmounts.getOrElse(i) { 0 } != 0L }) {
        CashflowSeries(Contribution, i, contribution.name, outflowColor(phases.size + i))
      } else {
        null
      }
    }
  val hasSurplus = rows.any { it.surplusSaved > 0 }
  val surplusSeries =
    CashflowSeries(Surplus, 0, "", outflowColor(phases.size + config.contributions.size))

  val inflows = potSeries + incomeSeries
  val outflows = buildList {
    addAll(phaseSeries)
    if (hasTax) +taxSeries
    addAll(contributionSeries)
    if (hasSurplus) +surplusSeries
  }
  val all = inflows + outflows

  fun indices(of: List<CashflowSeries>) = of.map(all::indexOf).toImmutableList()

  // Tax sits between the inflows and spending so the chain reads in order
  val groups = buildList {
    +CashflowGroup(Withdrawals, indices(potSeries), listMembers = true)
    if (incomeSeries.isNotEmpty()) +CashflowGroup(Income, indices(incomeSeries), true)
    if (hasTax) +CashflowGroup(Tax, indices(listOf(taxSeries)), listMembers = false)
    +CashflowGroup(Spending, indices(phaseSeries), listMembers = true)
    if (contributionSeries.isNotEmpty()) {
      +CashflowGroup(Contributions, indices(contributionSeries), listMembers = true)
    }
    if (hasSurplus) +CashflowGroup(Saved, indices(listOf(surplusSeries)), listMembers = false)
  }

  // Each series' position among the phases, or -1 for other kinds
  val phasePositions = all.map { series ->
    if (series.kind == Phase) phaseSeries.indexOf(series) else -1
  }
  val years = rows.map { row ->
    val age = config.currentAge + row.year - 1
    // The year's planned spend belongs to whichever phase is active
    val activePhase = phases.activeAt(age)
    val activePosition = phases.indexOfFirst { it === activePhase }
    CashflowYear(
      year = row.year,
      age = age,
      afterDepletion = row.afterDepletion,
      // Only income that actually left the plan counts as unspent
      unspentIncome = if (row.surplusSaved > 0) 0 else row.unspentIncome,
      amounts =
        all
          .mapIndexed { i, series -> row.amountFor(series, phasePositions[i] == activePosition) }
          .toImmutableList(),
    )
  }

  return CashflowChart(
    inflows = inflows.toImmutableList(),
    outflows = outflows.toImmutableList(),
    years = years.toImmutableList(),
    groups = groups.toImmutableList(),
  )
}

// Positive for money in, negative for money out
private fun McRunDetailRow.amountFor(series: CashflowSeries, isActivePhase: Boolean): Long =
  when (series.kind) {
    Pot -> {
      potWithdrawals.getOrElse(series.index) { 0 }
    }

    Income -> {
      incomeAmounts.getOrElse(series.index) { 0 }
    }

    Phase -> {
      if (isActivePhase) -plannedSpending else 0
    }

    Tax -> {
      -(taxPaid + incomeTax)
    }

    Contribution -> {
      -contributionAmounts.getOrElse(series.index) { 0 }
    }

    Surplus -> {
      -surplusSaved
    }
  }
