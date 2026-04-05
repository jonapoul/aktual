@file:Suppress("ComposeUnstableReceiver")

package aktual.core.ui

import aktual.budget.model.DateFormat
import aktual.core.l10n.Strings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char

@Composable
fun YearMonth.stringLong(): String {
  val month = month.stringLong()
  return "$month $year"
}

@Composable
fun YearMonth.stringShort(): String {
  val month = month.stringShort()
  return "$month $year"
}

@Composable
fun DayOfWeek.stringShort() =
  when (this) {
    DayOfWeek.MONDAY -> Strings.weekMon
    DayOfWeek.TUESDAY -> Strings.weekTue
    DayOfWeek.WEDNESDAY -> Strings.weekWed
    DayOfWeek.THURSDAY -> Strings.weekThu
    DayOfWeek.FRIDAY -> Strings.weekFri
    DayOfWeek.SATURDAY -> Strings.weekSat
    DayOfWeek.SUNDAY -> Strings.weekSun
  }

@Composable
fun Month.stringShort() =
  when (this) {
    Month.JANUARY -> Strings.monthJanShort
    Month.FEBRUARY -> Strings.monthFebShort
    Month.MARCH -> Strings.monthMarShort
    Month.APRIL -> Strings.monthAprShort
    Month.MAY -> Strings.monthMayShort
    Month.JUNE -> Strings.monthJunShort
    Month.JULY -> Strings.monthJulShort
    Month.AUGUST -> Strings.monthAugShort
    Month.SEPTEMBER -> Strings.monthSepShort
    Month.OCTOBER -> Strings.monthOctShort
    Month.NOVEMBER -> Strings.monthNovShort
    Month.DECEMBER -> Strings.monthDecShort
  }

@Composable
fun Month.stringLong() =
  when (this) {
    Month.JANUARY -> Strings.monthJanLong
    Month.FEBRUARY -> Strings.monthFebLong
    Month.MARCH -> Strings.monthMarLong
    Month.APRIL -> Strings.monthAprLong
    Month.MAY -> Strings.monthMayLong
    Month.JUNE -> Strings.monthJunLong
    Month.JULY -> Strings.monthJulLong
    Month.AUGUST -> Strings.monthAugLong
    Month.SEPTEMBER -> Strings.monthSepLong
    Month.OCTOBER -> Strings.monthOctLong
    Month.NOVEMBER -> Strings.monthNovLong
    Month.DECEMBER -> Strings.monthDecLong
  }

@Stable
fun DateFormat.formatter(): DateTimeFormat<LocalDate> =
  when (this) {
    DateFormat.MmDdYyyy -> MmDdYyyy
    DateFormat.DdMmYyyy -> DdMmYyyy
    DateFormat.YyyyMmDd -> YyyyMmDd
    DateFormat.MmDdYyyyDot -> MmDdYyyyDot
    DateFormat.DdMmYyyyDot -> DdMmYyyyDot
  }

private val MmDdYyyy = LocalDate.Format {
  monthNumber()
  char('/')
  day()
  char('/')
  year()
}
private val DdMmYyyy = LocalDate.Format {
  day()
  char('/')
  monthNumber()
  char('/')
  year()
}
private val YyyyMmDd = LocalDate.Format {
  year()
  char('-')
  monthNumber()
  char('-')
  day()
}
private val MmDdYyyyDot = LocalDate.Format {
  monthNumber()
  char('.')
  day()
  char('.')
  year()
}
private val DdMmYyyyDot = LocalDate.Format {
  day()
  char('.')
  monthNumber()
  char('.')
  year()
}
