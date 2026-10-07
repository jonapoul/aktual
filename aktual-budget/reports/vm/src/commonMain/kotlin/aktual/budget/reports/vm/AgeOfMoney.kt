package aktual.budget.reports.vm

import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus

// packages/desktop-client/src/components/reports/spreadsheets/age-of-money-spreadsheet.ts

internal data class AgeOfMoneyTransaction(val date: LocalDate, val amount: Long)

internal data class ExpenseAge(val date: LocalDate, val age: Int)

internal data class ExpenseAges(val ages: List<ExpenseAge>, val insufficientData: Boolean)

@Suppress("UseDataClass") private class IncomeBucket(val date: LocalDate, var remaining: Long)

private const val AVERAGE_COUNT = 10
private const val TREND_THRESHOLD_DAYS = 2

// FIFO: income becomes buckets, and each expense draws from the oldest bucket first. Its age is the
// number of days since the last bucket it drew from
internal fun calculateAges(transactions: List<AgeOfMoneyTransaction>): ExpenseAges {
  val buckets =
    transactions
      .asSequence()
      .filter { it.amount > 0 }
      .sortedBy { it.date }
      .map { IncomeBucket(it.date, it.amount) }
      .toList()
  val expenses = transactions.filter { it.amount < 0 }.sortedBy { it.date }

  val ages = mutableListOf<ExpenseAge>()
  var bucketIndex = 0
  var insufficientData = false

  for (expense in expenses) {
    var remaining = -expense.amount
    var lastBucketDate: LocalDate? = null

    while (remaining > 0 && bucketIndex < buckets.size) {
      val bucket = buckets[bucketIndex]
      if (bucket.remaining > 0) {
        val deduction = minOf(bucket.remaining, remaining)
        bucket.remaining -= deduction
        remaining -= deduction
        lastBucketDate = bucket.date
      }
      if (bucket.remaining <= 0) bucketIndex++
    }

    if (remaining > 0) insufficientData = true

    if (lastBucketDate != null) {
      val age = lastBucketDate.daysUntil(expense.date).coerceAtLeast(0)
      ages.add(ExpenseAge(expense.date, age))
    }
  }

  return ExpenseAges(ages, insufficientData)
}

internal fun averageAge(ages: List<Int>, count: Int = AVERAGE_COUNT): Int? =
  if (ages.isEmpty()) null else ages.takeLast(count).average().roundToInt()

// Each period holds the average of the last 10 expense ages up to the end of it. Daily and weekly
// periods stop at today, so the chart doesn't flatline into the future
internal fun calculateGraphData(
  ages: List<ExpenseAge>,
  start: YearMonth,
  end: YearMonth,
  granularity: AgeOfMoneyGranularity,
  today: LocalDate,
): ImmutableMap<LocalDate, Int> {
  val endDate =
    when (granularity) {
      Daily,
      Weekly -> minOf(end.lastDay, today)
      Monthly,
      Unknown -> end.lastDay
    }

  val agesByPeriod = ages.groupBy({ it.date.periodStart(granularity) }, { it.age })
  val agesSoFar = mutableListOf<Int>()
  val result = linkedMapOf<LocalDate, Int>()

  var period = start.firstDay.periodStart(granularity)
  while (period <= endDate) {
    agesByPeriod[period]?.let(agesSoFar::addAll)
    averageAge(agesSoFar)?.let { result[period] = it }
    period = period.plus(granularity.step())
  }

  return result.toImmutableMap()
}

internal fun calculateTrend(values: List<Int>): AgeOfMoneyTrend {
  if (values.size < 2) return Stable
  val diff = values[values.size - 1] - values[values.size - 2]
  return when {
    diff > TREND_THRESHOLD_DAYS -> Up
    diff < -TREND_THRESHOLD_DAYS -> Down
    else -> Stable
  }
}

// Weeks start on Monday, as upstream
internal fun LocalDate.periodStart(granularity: AgeOfMoneyGranularity): LocalDate =
  when (granularity) {
    Daily -> this
    Weekly -> minus(dayOfWeek.ordinal, DAY)
    Monthly,
    Unknown -> LocalDate(year, month, 1)
  }

private fun AgeOfMoneyGranularity.step(): DatePeriod =
  when (this) {
    Daily -> DatePeriod(days = 1)
    Weekly -> DatePeriod(days = 7)
    Monthly,
    Unknown -> DatePeriod(months = 1)
  }
