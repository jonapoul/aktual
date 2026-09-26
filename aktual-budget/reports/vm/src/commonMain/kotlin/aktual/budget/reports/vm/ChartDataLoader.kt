package aktual.budget.reports.vm

import aktual.budget.db.dao.ReportsDao
import aktual.budget.db.reports.CashFlowByMonth
import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.core.Calendar
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlinx.datetime.yearMonth

@Inject
@OptIn(ExperimentalCoroutinesApi::class)
internal class ChartDataLoader(private val dao: ReportsDao, private val calendar: Calendar) {
  fun text(meta: MarkdownReportMeta): Flow<ChartData> = flowOf(TextData(meta.content))

  // packages/desktop-client/src/components/reports/spreadsheets/cash-flow-spreadsheet.tsx
  // cashFlowByDate()
  fun cashFlow(meta: CashFlowReportMeta): Flow<ChartData> {
    if (meta.conditions.hasFilters()) return flowOf(UnsupportedData(Filters))

    return dao.observeTransactionDateBounds().flatMapLatest { bounds ->
      val today = calendar.today()
      val range =
        resolveTimeRange(
          timeFrame = meta.timeFrame,
          default = TimeFrame(start = today.yearMonth, end = today.yearMonth, mode = SlidingWindow),
          today = today,
          latestTransaction = bounds.latest,
        )
      val start = range.start
      val end = minOf(range.endInclusive, today.yearMonth)

      combine(
        dao.observeCashFlowStartingBalance(start.firstDay),
        dao.observeCashFlowByMonth(start.firstDay, end.lastDay),
      ) { startingBalance, rows ->
        val byMonth = rows.groupBy { it.month }
        var balance = Amount(startingBalance)
        val items =
          (start..end).associateWith { month ->
            val monthRows = byMonth[month.toLong()].orEmpty()
            val income = monthRows.total { !it.is_transfer && it.is_income }
            val expenses = monthRows.total { !it.is_transfer && !it.is_income }
            val transfers = monthRows.total { it.is_transfer }
            balance += income + expenses + transfers
            CashFlowDatum(income, expenses, transfers, balance)
          }
        CashFlowData(title = meta.name, items = items.toImmutableMap())
      }
    }
  }

  // packages/desktop-client/src/components/reports/spreadsheets/net-worth-spreadsheet.ts
  // createSpreadsheet()
  fun netWorth(meta: NetWorthReportMeta): Flow<ChartData> {
    if (meta.conditions.hasFilters()) return flowOf(UnsupportedData(Filters))

    return dao.observeTransactionDateBounds().flatMapLatest { bounds ->
      val today = calendar.today()
      val range =
        resolveTimeRange(
          timeFrame = meta.timeFrame,
          default = null,
          today = today,
          latestTransaction = bounds.latest,
        )

      // Go back one month to show the change into the first month, unless there's no data before it
      val earliest = bounds.earliest
      val start =
        if (earliest != null && earliest >= range.start.firstDay) range.start
        else range.start.minusMonth()
      val end = range.endInclusive

      combine(
        dao.observeNetWorthStartingBalance(start.firstDay),
        dao.observeNetWorthByMonth(start.firstDay, end.lastDay),
      ) { startingBalance, rows ->
        val byMonth = rows.associate { it.month to it.total }
        var balance = Amount(startingBalance)
        val items =
          (start..end).associateWith { month ->
            balance += Amount(byMonth[month.toLong()] ?: 0L)
            balance
          }
        NetWorthData(title = meta.name, items = items.toImmutableMap())
      }
    }
  }

  private fun List<Condition>?.hasFilters() = !isNullOrEmpty()

  private fun YearMonth.toLong(): Long = year * YEAR_MONTH_FACTOR + month.ordinal + 1L

  private inline fun List<CashFlowByMonth>.total(predicate: (CashFlowByMonth) -> Boolean): Amount =
    Amount(filter(predicate).sumOf { it.total })

  private companion object {
    const val YEAR_MONTH_FACTOR = 100L
  }
}
