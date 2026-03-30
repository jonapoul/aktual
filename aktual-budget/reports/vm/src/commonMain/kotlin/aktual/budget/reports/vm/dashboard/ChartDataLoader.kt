@file:Suppress("MagicNumber")

package aktual.budget.reports.vm.dashboard

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.withResult
import aktual.budget.model.Amount
import aktual.budget.model.ResolvedDateRange
import aktual.budget.reports.vm.CalendarData
import aktual.budget.reports.vm.CalendarDay
import aktual.budget.reports.vm.CalendarMonth
import aktual.budget.reports.vm.CashFlowData
import aktual.budget.reports.vm.CashFlowDatum
import aktual.budget.reports.vm.NetWorthData
import aktual.budget.reports.vm.SummaryChartType
import aktual.budget.reports.vm.SummaryData
import aktual.budget.reports.vm.SummaryData.AveragePerMonth
import aktual.budget.reports.vm.SummaryData.AveragePerTransaction
import aktual.budget.reports.vm.SummaryData.AveragePerYear
import aktual.budget.reports.vm.SummaryData.Sum
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number

internal class ChartDataLoader(database: BudgetDatabase) {
  private val queries = database.transactionsQueries

  suspend fun loadCashFlow(title: String, range: ResolvedDateRange): CashFlowData {
    val rows = queries.withResult { cashFlowByMonth(range.start, range.end).executeAsList() }
    var runningBalance = Amount.Zero
    val items = linkedMapOf<YearMonth, CashFlowDatum>()
    for (row in rows) {
      val ym = row.yearMonth?.toYearMonth() ?: continue
      val income = row.income.toAmount()
      val expenses = row.expenses.toAmount()
      val transfers = row.transfers.toAmount()
      runningBalance += income + expenses + transfers
      items[ym] = CashFlowDatum(income, expenses, transfers, runningBalance)
    }
    return CashFlowData(title, items.toImmutableMap())
  }

  suspend fun loadNetWorth(title: String, range: ResolvedDateRange): NetWorthData {
    val startingBalance = queries.withResult {
      netWorthBalanceBefore(range.start).executeAsOneOrNull()?.balance.toAmount()
    }
    val changes = queries.withResult {
      netWorthChangesByMonth(range.start, range.end).executeAsList()
    }
    var running = startingBalance
    val items = linkedMapOf<YearMonth, Amount>()
    for (row in changes) {
      val ym = row.yearMonth?.toYearMonth() ?: continue
      running += row.change.toAmount()
      items[ym] = running
    }
    return NetWorthData(title, items.toImmutableMap())
  }

  suspend fun loadCalendar(title: String, range: ResolvedDateRange): CalendarData {
    val rows = queries.withResult { calendarByDay(range.start, range.end).executeAsList() }

    val byMonth = linkedMapOf<YearMonth, MutableList<CalendarDay>>()
    val monthIncome = mutableMapOf<YearMonth, Amount>()
    val monthExpenses = mutableMapOf<YearMonth, Amount>()

    for (row in rows) {
      val date = row.date ?: continue
      val ym = YearMonth(date.year, date.month)
      val inc = row.income.toAmount()
      val exp = row.expenses.toAmount()
      byMonth.getOrPut(ym) { mutableListOf() }.add(CalendarDay(date.day, inc, exp))
      monthIncome[ym] = (monthIncome[ym] ?: Amount.Zero) + inc
      monthExpenses[ym] = (monthExpenses[ym] ?: Amount.Zero) + exp
    }

    var totalIncome = Amount.Zero
    var totalExpenses = Amount.Zero
    val months =
      byMonth
        .map { (ym, days) ->
          val mi = monthIncome[ym] ?: Amount.Zero
          val me = monthExpenses[ym] ?: Amount.Zero
          totalIncome += mi
          totalExpenses += me
          CalendarMonth(mi, me, ym, days.toImmutableList())
        }
        .toImmutableList()

    val startYm = YearMonth(range.start.year, range.start.month)
    val endYm = YearMonth(range.end.year, range.end.month)
    return CalendarData(title, startYm, endYm, totalIncome, totalExpenses, months)
  }

  suspend fun loadSummary(
    title: String,
    range: ResolvedDateRange,
    type: SummaryChartType,
  ): SummaryData {
    val row = queries.withResult { summaryInRange(range.start, range.end).executeAsOne() }
    val total = row.total.toAmount()
    val count = row.transactionCount.toInt()

    return when (type) {
      SummaryChartType.Sum -> {
        Sum(title, range.start, range.end, total)
      }
      SummaryChartType.AveragePerMonth -> {
        val numMonths = monthsBetween(range.start, range.end)
        val avg = if (numMonths > 0f) Amount((total.toLong() / numMonths).toLong()) else Amount.Zero
        AveragePerMonth(title, range.start, range.end, numMonths, total, avg)
      }
      SummaryChartType.AveragePerTransaction -> {
        val avg = if (count > 0) Amount(total.toLong() / count) else Amount.Zero
        AveragePerTransaction(title, range.start, range.end, count, total, avg)
      }
      SummaryChartType.Percentage -> {
        Sum(title, range.start, range.end, total)
      }
      SummaryChartType.AveragePerYear -> {
        val numYears = yearsBetween(range.start, range.end)
        val avg = if (numYears > 0f) Amount((total.toLong() / numYears).toLong()) else Amount.Zero
        AveragePerYear(title, range.start, range.end, numYears, total, avg)
      }
    }
  }
}

private fun Double?.toAmount(): Amount = if (this == null) Amount.Zero else Amount(toLong())

private fun Long.toYearMonth(): YearMonth {
  val month = (this % 100).toInt()
  val year = (this / 100).toInt()
  return YearMonth(year, Month(month))
}

private fun monthsBetween(start: LocalDate, end: LocalDate): Float {
  val startMonths = start.year * 12 + start.month.number
  val endMonths = end.year * 12 + end.month.number
  return (endMonths - startMonths + 1).toFloat()
}

private fun yearsBetween(start: LocalDate, end: LocalDate): Float = monthsBetween(start, end) / 12f
