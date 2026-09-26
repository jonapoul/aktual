package aktual.budget.reports.vm

import aktual.budget.model.Amount
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plusMonth

private const val MONTHS_PER_YEAR = 12
private const val MAX_PROJECTION_MONTHS = 600
private const val HAMPEL_THRESHOLD = 3
private const val MAD_SCALE = 1.4826

internal data class CrossoverParams(
  val safeWithdrawalRate: Double,
  val estimatedReturn: Double?,
  val expectedContribution: Long?,
  val projectionType: ProjectionType,
  val expenseAdjustmentFactor: Double,
)

// packages/desktop-client/src/components/reports/spreadsheets/crossover-spreadsheet.ts
// recalculate()
// expenses and balances line up with months. Expenses are positive spend, balances are at the end
// of each month.
internal fun calculateCrossover(
  title: String?,
  months: List<YearMonth>,
  expenses: List<Long>,
  balances: List<Long>,
  params: CrossoverParams,
  today: LocalDate,
): CrossoverData {
  val monthlySwr = params.safeWithdrawalRate / MONTHS_PER_YEAR
  val items = LinkedHashMap<YearMonth, CrossoverDatum>()
  months.forEachIndexed { i, month ->
    items[month] =
      CrossoverDatum(
        investmentIncome = Amount((balances[i] * monthlySwr).roundToLong()),
        expenses = Amount(expenses[i]),
        nestEgg = Amount(balances[i]),
      )
  }

  // Crossover is only checked against projected expenses, so a short dip in historical spending
  // doesn't count
  var crossover: YearMonth? = null
  if (months.isNotEmpty()) {
    val monthlyReturn =
      params.estimatedReturn?.let { (1 + it).pow(1.0 / MONTHS_PER_YEAR) - 1 }
        ?: historicalMonthlyReturn(balances)
        ?: 0.0
    val contribution = params.expectedContribution ?: 0L
    val values = expenses.map { it.toDouble() }
    val flatExpense =
      when (params.projectionType) {
        Hampel,
        Unknown -> hampelFilteredMedian(values)
        Median -> median(values)
        Mean -> values.average().takeUnless { it.isNaN() } ?: 0.0
      }
    val projectedExpenses = max(0.0, flatExpense).roundToLong()
    val adjustedExpenses = (max(0.0, flatExpense) * params.expenseAdjustmentFactor).roundToLong()

    var balance = balances.last().toDouble()
    var month = months.last()
    val maxItems = months.size + MAX_PROJECTION_MONTHS
    while (crossover == null && items.size < maxItems) {
      month = month.plusMonth()
      balance = (balance + contribution) * (1 + monthlyReturn)
      val income = (balance * monthlySwr).roundToLong()
      items[month] =
        CrossoverDatum(
          investmentIncome = Amount(income),
          expenses = Amount(projectedExpenses),
          nestEgg = Amount(balance.roundToLong()),
          adjustedExpenses = Amount(adjustedExpenses),
        )
      if (income >= adjustedExpenses) crossover = month
    }
  }

  val yearsToRetire = crossover?.let {
    max(0, today.monthsUntil(it.firstDay)).toDouble() / MONTHS_PER_YEAR
  }

  return CrossoverData(
    title = title,
    items = items.toImmutableMap(),
    crossover = crossover,
    yearsToRetire = yearsToRetire,
  )
}

// Monthly CAGR between the first non-zero balance and the last one
private fun historicalMonthlyReturn(balances: List<Long>): Double? {
  if (balances.size < 2) return null
  val first = balances.firstOrNull { it != 0L } ?: 0L
  val last = balances.last()
  val n = balances.size - 1
  if (first <= 0 || last <= 0) return 0.0
  val cagr = (last.toDouble() / first).pow(1.0 / n) - 1
  return if (cagr.isFinite()) cagr else 0.0
}

internal fun median(values: List<Double>): Double {
  if (values.isEmpty()) return 0.0
  val sorted = values.sorted()
  val mid = sorted.size / 2
  return if (sorted.size % 2 == 0) (sorted[mid - 1] + sorted[mid]) / 2 else sorted[mid]
}

// Drops outliers more than 3 scaled median absolute deviations from the median
internal fun hampelFilteredMedian(values: List<Double>): Double {
  if (values.size <= 1) return values.firstOrNull() ?: 0.0
  val median = median(values)
  val mad = median(values.map { abs(it - median) })
  val bound = MAD_SCALE * mad * HAMPEL_THRESHOLD
  return median(values.filter { abs(it - median) <= bound })
}
