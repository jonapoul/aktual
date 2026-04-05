package aktual.budget.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.DateTimeUnit.Companion.MONTH
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

@Immutable data class ResolvedDateRange(val start: LocalDate, val end: LocalDate)

@Suppress("MagicNumber")
fun DateRangeType.resolve(today: LocalDate): ResolvedDateRange {
  val firstOfMonth = LocalDate(today.year, today.month, 1)
  return when (this) {
    DateRangeType.ThisWeek -> {
      val daysSinceMonday = today.dayOfWeek.ordinal
      ResolvedDateRange(today.minus(daysSinceMonday, DAY), today)
    }
    DateRangeType.LastWeek -> {
      val daysSinceMonday = today.dayOfWeek.ordinal
      val thisMonday = today.minus(daysSinceMonday, DAY)
      val lastMonday = thisMonday.minus(7, DAY)
      ResolvedDateRange(lastMonday, thisMonday.minus(1, DAY))
    }
    DateRangeType.ThisMonth -> {
      ResolvedDateRange(firstOfMonth, today)
    }
    DateRangeType.LastMonth -> {
      val lastMonthStart = firstOfMonth.minus(1, MONTH)
      ResolvedDateRange(lastMonthStart, firstOfMonth.minus(1, DAY))
    }
    DateRangeType.Last3Months -> {
      ResolvedDateRange(firstOfMonth.minus(2, MONTH), today)
    }
    DateRangeType.Last6Months -> {
      ResolvedDateRange(firstOfMonth.minus(5, MONTH), today)
    }
    DateRangeType.Last12Months -> {
      ResolvedDateRange(firstOfMonth.minus(11, MONTH), today)
    }
    DateRangeType.YearToDate -> {
      ResolvedDateRange(LocalDate(today.year, 1, 1), today)
    }
    DateRangeType.LastYear -> {
      ResolvedDateRange(LocalDate(today.year - 1, 1, 1), LocalDate(today.year - 1, 12, 31))
    }
    DateRangeType.AllTime -> {
      ResolvedDateRange(LocalDate(2000, 1, 1), today)
    }
  }
}
