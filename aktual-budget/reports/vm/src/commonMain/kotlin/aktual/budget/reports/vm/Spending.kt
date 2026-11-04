package aktual.budget.reports.vm

import aktual.budget.db.ReportTransactions
import aktual.budget.model.Amount
import kotlin.math.roundToLong
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.minusMonth
import kotlinx.datetime.onDay
import kotlinx.datetime.yearMonth

// packages/desktop-client/src/components/reports/spreadsheets/spending-spreadsheet.ts

internal data class SpendingMonths(val compare: YearMonth, val compareTo: YearMonth)

internal val SpendingReportMeta.spendingMode: SpendingMode
  get() = mode?.takeIf { it != Unknown } ?: SingleMonth

internal val SpendingReportMeta.dateRangeMode: DateRangeMode
  get() = if (isLive == false) Static else Live

// packages/desktop-client/src/components/reports/reportRanges.ts calculateSpendingReportTimeRange()
internal fun SpendingReportMeta.resolveMonths(today: LocalDate): SpendingMonths {
  val current = today.yearMonth
  // Budget and average chart a single month, so a live report is always the current month
  if (spendingMode != SingleMonth && dateRangeMode == Live) return SpendingMonths(current, current)

  val compare = compare?.toYearMonthOrNull()
  val compareTo = compareTo?.toYearMonthOrNull()
  val timeFrame =
    TimeFrame(
      start = compare ?: current,
      end = compareTo ?: (compare ?: current).minusMonth(),
      mode = if (dateRangeMode == Live) SlidingWindow else Static,
    )
  val range = resolveTimeRange(timeFrame, default = null, today, latestTransaction = null)
  return SpendingMonths(range.start, range.endInclusive)
}

// packages/desktop-client/src/components/reports/spendingAverageRange.ts
// resolveSpendingAverageRange(): the months before [compare] that the average covers
internal fun SpendingAverageRange?.resolve(
  compare: YearMonth,
  earliest: YearMonth?,
): List<YearMonth> {
  val end = compare.minusMonth()
  val start =
    when (val range = this ?: Default) {
      is LastMonths -> compare.minus(range.months, MONTH)
      YearToDate -> YearMonth(compare.year, JANUARY)
      AllTime -> earliest
    }
  return if (start == null || start > end) emptyList() else (start..end).toList()
}

// Each day's total of the transactions spending counts: no income categories or off-budget accounts
internal fun spendingTotals(rows: List<ReportTransactions>): Map<LocalDate, Long> =
  rows
    .filter { it.is_income != true && it.off_budget != true }
    .groupBy { it.date }
    .mapValues { (_, group) -> group.sumOf { it.amount } }

internal data class SpendingParams(
  val title: String,
  val dateRangeMode: DateRangeMode,
  val months: SpendingMonths,
  val mode: SpendingMode,
  val averageMonths: List<YearMonth>,
  val today: LocalDate,
)

/**
 * Days 1 to 27 each hold the spending so far that month, and the last day holds the 28th to the end
 * of the month. Spending reads as positive, so a month that's spent 100 holds 100. [budget] is the
 * target month's budgeted total for the categories the report covers, only read in budget mode.
 */
internal fun calculateSpending(
  params: SpendingParams,
  totals: Map<LocalDate, Long>,
  budget: Long,
): SpendingData {
  val (compare, compareTo) = params.months
  val running = RunningTotals(totals)
  val target = { day: Int -> running.spentBy(compare, day, params.today) }
  val comparison: (Int) -> Long? =
    when (params.mode) {
      Budget -> { day ->
        budgetBy(compare, day, budget)
      }
      Average -> { day ->
        running.averageBy(params.averageMonths, day)
      }
      SingleMonth,
      Unknown -> { day ->
        running.spentBy(compareTo, day, params.today)
      }
    }

  // SpendingCard.tsx difference: on today's day, or on the last day for any other month
  val differenceDay =
    if (compare == params.today.yearMonth) minOf(params.today.day, END_DAY) else END_DAY
  val difference = (comparison(differenceDay) ?: 0L) - (target(differenceDay) ?: 0L)

  return SpendingData(
    title = params.title,
    mode = params.dateRangeMode,
    targetMonth = compare,
    comparison =
      when (params.mode) {
        Budget -> SpendingComparison.Budgeted
        Average -> SpendingComparison.Average
        SingleMonth,
        Unknown -> SpendingComparison.SingleMonth(compareTo)
      },
    difference = Amount(difference),
    days =
      (1..END_DAY)
        .map { day ->
          SpendingDay(
            number = if (day == END_DAY) SpendingDayNumber.End else SpendingDayNumber.Specific(day),
            target = target(day)?.let { Amount(-it) },
            comparison = comparison(day)?.let { Amount(-it) },
          )
        }
        .toImmutableList(),
  )
}

// The budget spread evenly over the month, as a running total
private fun budgetBy(month: YearMonth, day: Int, budget: Long): Long {
  val days = month.numberOfDays
  val elapsed = if (day == END_DAY) days else day
  return (-budget.toDouble() * elapsed / days).roundToLong()
}

private class RunningTotals(private val totals: Map<LocalDate, Long>) {
  private val cache = mutableMapOf<YearMonth, LongArray>()

  // Index 0 is the 1st of the month
  private fun of(month: YearMonth): LongArray =
    cache.getOrPut(month) {
      var sum = 0L
      LongArray(month.numberOfDays) { index ->
        sum += totals[month.onDay(index + 1)] ?: 0L
        sum
      }
    }

  // Null for a day that hasn't happened yet. The last day reads the latest of the 28th onwards
  // that has
  fun spentBy(month: YearMonth, day: Int, today: LocalDate): Long? {
    val lastDay = if (day == END_DAY) month.numberOfDays else day
    val reached = (lastDay downTo day).firstOrNull { month.onDay(it) <= today } ?: return null
    return of(month)[reached - 1]
  }

  fun averageBy(months: List<YearMonth>, day: Int): Long {
    if (months.isEmpty()) return 0L
    val sum = months.sumOf { month ->
      val lastDay = if (day == END_DAY) month.numberOfDays else day
      of(month)[lastDay - 1]
    }
    return (sum.toDouble() / months.size).roundToLong()
  }
}

private const val END_DAY = 28
