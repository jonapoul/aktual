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
    MONDAY -> Strings.weekMon
    TUESDAY -> Strings.weekTue
    WEDNESDAY -> Strings.weekWed
    THURSDAY -> Strings.weekThu
    FRIDAY -> Strings.weekFri
    SATURDAY -> Strings.weekSat
    SUNDAY -> Strings.weekSun
  }

@Composable
fun Month.stringShort() =
  when (this) {
    JANUARY -> Strings.monthJanShort
    FEBRUARY -> Strings.monthFebShort
    MARCH -> Strings.monthMarShort
    APRIL -> Strings.monthAprShort
    MAY -> Strings.monthMayShort
    JUNE -> Strings.monthJunShort
    JULY -> Strings.monthJulShort
    AUGUST -> Strings.monthAugShort
    SEPTEMBER -> Strings.monthSepShort
    OCTOBER -> Strings.monthOctShort
    NOVEMBER -> Strings.monthNovShort
    DECEMBER -> Strings.monthDecShort
  }

@Composable
fun Month.stringLong() =
  when (this) {
    JANUARY -> Strings.monthJanLong
    FEBRUARY -> Strings.monthFebLong
    MARCH -> Strings.monthMarLong
    APRIL -> Strings.monthAprLong
    MAY -> Strings.monthMayLong
    JUNE -> Strings.monthJunLong
    JULY -> Strings.monthJulLong
    AUGUST -> Strings.monthAugLong
    SEPTEMBER -> Strings.monthSepLong
    OCTOBER -> Strings.monthOctLong
    NOVEMBER -> Strings.monthNovLong
    DECEMBER -> Strings.monthDecLong
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
