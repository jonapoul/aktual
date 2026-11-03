package aktual.budget.reports.vm

import aktual.budget.db.ReportTransactions
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.Interval
import aktual.budget.model.sum
import aktual.budget.model.sumOf
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.YearMonthRange
import kotlinx.datetime.minus
import kotlinx.datetime.minusMonth
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth

// Keyed by the first day of each period
@Immutable
data class NetWorthAccount(
  val id: AccountId,
  val name: String,
  val balances: ImmutableMap<LocalDate, Amount>,
)

// One account's change in a period, or its balance before the range when period is null
internal data class NetWorthRow(
  val account: AccountId,
  val name: String?,
  val period: LocalDate?,
  val total: Long,
)

// The periods a net worth report shows, from one period before the range so the first one shows
// its change, unless there's no data before it. Daily and weekly periods stop at today
internal data class NetWorthPeriods(
  val interval: Interval,
  val start: LocalDate,
  val end: LocalDate,
  val firstDayOfWeek: FirstDayOfWeek,
) {
  fun periodOf(date: LocalDate): LocalDate =
    when (interval) {
      Daily -> date
      Weekly -> weekStart(date, firstDayOfWeek)
      Yearly -> LocalDate(date.year, JANUARY, 1)
      Monthly,
      Unknown -> date.yearMonth.firstDay
    }

  fun keys(): List<LocalDate> =
    generateSequence(periodOf(start)) { it.plus(step()) }.takeWhile { it <= end }.toList()

  private fun step(): DatePeriod =
    when (interval) {
      Daily -> DatePeriod(days = 1)
      Weekly -> DatePeriod(days = DAYS_PER_WEEK)
      Yearly -> DatePeriod(years = 1)
      Monthly,
      Unknown -> DatePeriod(months = 1)
    }

  companion object {
    // packages/desktop-client/src/components/reports/spreadsheets/net-worth-spreadsheet.ts
    // createSpreadsheet()
    fun of(
      interval: Interval,
      range: YearMonthRange,
      earliest: LocalDate?,
      today: LocalDate,
      firstDayOfWeek: FirstDayOfWeek,
    ): NetWorthPeriods {
      val rangeStart = range.start.firstDay
      // Set when there's no data before the range, so there's no period before it to show
      val firstData = earliest?.takeIf { it >= rangeStart }
      val start =
        when (interval) {
          Daily -> firstData ?: rangeStart.minus(1, DAY)
          Weekly -> weekStart(firstData ?: rangeStart.minus(1, DAY), firstDayOfWeek)
          Monthly,
          Yearly,
          Unknown -> if (firstData != null) rangeStart else range.start.minusMonth().firstDay
        }
      val end =
        when (interval) {
          Daily,
          Weekly -> minOf(range.endInclusive.lastDay, today)
          Monthly,
          Yearly,
          Unknown -> range.endInclusive.lastDay
        }
      return NetWorthPeriods(interval, start, end, firstDayOfWeek)
    }
  }
}

// The netWorthByAccount query run over transactions that matched a report's conditions, grouped
// into the report's periods
internal fun netWorthRows(
  rows: List<ReportTransactions>,
  periods: NetWorthPeriods,
): List<NetWorthRow> =
  rows
    .filter { it.account_tombstone == false && it.date <= periods.end }
    .map { row ->
      NetWorthRow(
        account = row.account,
        name = row.account_name,
        period = if (row.date < periods.start) null else periods.periodOf(row.date),
        total = row.amount,
      )
    }

// packages/desktop-client/src/components/reports/spreadsheets/net-worth-spreadsheet.ts
// recalculate()
// and NetWorthGraph.tsx: each account's running balance, leaving out accounts that stay at zero, in
// stacking order (smallest total over the range first)
internal fun netWorthData(
  title: String?,
  mode: NetWorthMode?,
  interval: Interval,
  periods: List<LocalDate>,
  rows: List<NetWorthRow>,
): NetWorthData {
  val accounts =
    rows
      .groupBy { it.account }
      .entries
      .asSequence()
      .map { (id, accountRows) ->
        val changes =
          accountRows
            .filter { it.period != null }
            .groupingBy { it.period }
            .fold(0L) { total, row -> total + row.total }
        var balance = accountRows.filter { it.period == null }.map { it.total }.sum()
        val balances = periods.associateWith { period ->
          balance += changes[period] ?: 0L
          Amount(balance)
        }
        NetWorthAccount(
          id = id,
          name = accountRows.firstNotNullOfOrNull { it.name }.orEmpty(),
          balances = balances.toImmutableMap(),
        )
      }
      .filter { account -> account.balances.values.any { it != Zero } }
      .sortedWith(
        compareBy<NetWorthAccount> { account -> account.balances.values.sum() }.thenBy { it.name },
      )
      .toList()

  val items = periods.associateWith { period ->
    accounts.sumOf { it.balances.getValue(period) }
  }

  return NetWorthData(
    title = title,
    items = items.toImmutableMap(),
    mode = if (mode == Stacked) Stacked else Trend,
    interval = interval,
    accounts = accounts.toImmutableList(),
  )
}
