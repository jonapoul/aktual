package actual.budget.reports.ui.charts.calendar

import actual.l10n.Strings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

@Composable
@ReadOnlyComposable
internal fun stringLong(yearMonth: YearMonth): String {
  val month = when (yearMonth.month) {
    Month.JANUARY -> Strings.reportsMonthJanL
    Month.FEBRUARY -> Strings.reportsMonthFebL
    Month.MARCH -> Strings.reportsMonthMarL
    Month.APRIL -> Strings.reportsMonthAprL
    Month.MAY -> Strings.reportsMonthMayL
    Month.JUNE -> Strings.reportsMonthJunL
    Month.JULY -> Strings.reportsMonthJulL
    Month.AUGUST -> Strings.reportsMonthAugL
    Month.SEPTEMBER -> Strings.reportsMonthSepL
    Month.OCTOBER -> Strings.reportsMonthOctL
    Month.NOVEMBER -> Strings.reportsMonthNovL
    Month.DECEMBER -> Strings.reportsMonthDecL
  }
  return "$month ${yearMonth.year}"
}

@Composable
@ReadOnlyComposable
internal fun stringShort(yearMonth: YearMonth): String {
  val month = when (yearMonth.month) {
    Month.JANUARY -> Strings.reportsMonthJan
    Month.FEBRUARY -> Strings.reportsMonthFeb
    Month.MARCH -> Strings.reportsMonthMar
    Month.APRIL -> Strings.reportsMonthApr
    Month.MAY -> Strings.reportsMonthMay
    Month.JUNE -> Strings.reportsMonthJun
    Month.JULY -> Strings.reportsMonthJul
    Month.AUGUST -> Strings.reportsMonthAug
    Month.SEPTEMBER -> Strings.reportsMonthSep
    Month.OCTOBER -> Strings.reportsMonthOct
    Month.NOVEMBER -> Strings.reportsMonthNov
    Month.DECEMBER -> Strings.reportsMonthDec
  }
  return "$month ${yearMonth.year}"
}

@Composable
@ReadOnlyComposable
internal fun stringShort(day: DayOfWeek) = when (day) {
  DayOfWeek.MONDAY -> Strings.reportsWeekMon
  DayOfWeek.TUESDAY -> Strings.reportsWeekTue
  DayOfWeek.WEDNESDAY -> Strings.reportsWeekWed
  DayOfWeek.THURSDAY -> Strings.reportsWeekThu
  DayOfWeek.FRIDAY -> Strings.reportsWeekFri
  DayOfWeek.SATURDAY -> Strings.reportsWeekSat
  DayOfWeek.SUNDAY -> Strings.reportsWeekSun
}
