package aktual.budget.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

data class ResolvedDateRange(val start: LocalDate, val end: LocalDate)

@Suppress("MagicNumber")
fun DateRangeType.resolve(today: LocalDate): ResolvedDateRange {
  val firstOfMonth = LocalDate(today.year, today.month, 1)
  return when (this) {
    ThisWeek -> {
      val daysSinceMonday = today.dayOfWeek.ordinal
      ResolvedDateRange(today.minus(daysSinceMonday, DAY), today)
    }
    LastWeek -> {
      val daysSinceMonday = today.dayOfWeek.ordinal
      val thisMonday = today.minus(daysSinceMonday, DAY)
      val lastMonday = thisMonday.minus(7, DAY)
      ResolvedDateRange(lastMonday, thisMonday.minus(1, DAY))
    }
    ThisMonth -> {
      ResolvedDateRange(firstOfMonth, today)
    }
    LastMonth -> {
      val lastMonthStart = firstOfMonth.minus(1, MONTH)
      ResolvedDateRange(lastMonthStart, firstOfMonth.minus(1, DAY))
    }
    CurrentQuarter -> {
      val quarterStart = firstOfMonth.minus(today.month.ordinal % 3, MONTH)
      ResolvedDateRange(quarterStart, quarterStart.plus(3, MONTH).minus(1, DAY))
    }
    PreviousQuarter -> {
      val quarterStart = firstOfMonth.minus(today.month.ordinal % 3, MONTH)
      ResolvedDateRange(quarterStart.minus(3, MONTH), quarterStart.minus(1, DAY))
    }
    Last30Days -> {
      ResolvedDateRange(today.minus(29, DAY), today)
    }
    Last3Months -> {
      ResolvedDateRange(firstOfMonth.minus(2, MONTH), today)
    }
    Last6Months -> {
      ResolvedDateRange(firstOfMonth.minus(5, MONTH), today)
    }
    Last12Months -> {
      ResolvedDateRange(firstOfMonth.minus(11, MONTH), today)
    }
    YearToDate -> {
      ResolvedDateRange(LocalDate(today.year, 1, 1), today)
    }
    LastYear -> {
      ResolvedDateRange(LocalDate(today.year - 1, 1, 1), LocalDate(today.year - 1, 12, 31))
    }
    PriorYearToDate -> {
      ResolvedDateRange(LocalDate(today.year - 1, 1, 1), today.minus(1, YEAR))
    }
    AllTime,
    Unknown -> {
      ResolvedDateRange(LocalDate(2000, 1, 1), today)
    }
  }
}
