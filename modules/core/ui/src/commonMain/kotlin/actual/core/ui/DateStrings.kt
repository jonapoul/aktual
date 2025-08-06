// YearAndMonth and DayOfWeek are set to stable in compose-stability.conf
@file:SuppressLint("ComposeUnstableReceiver")

package actual.core.ui

import actual.l10n.Res
import actual.l10n.week_fri
import actual.l10n.week_mon
import actual.l10n.week_sat
import actual.l10n.week_sun
import actual.l10n.week_thu
import actual.l10n.week_tue
import actual.l10n.week_wed
import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.jetbrains.compose.resources.stringResource

@Composable
@ReadOnlyComposable
fun YearMonth.stringLong(): String {
  val month = month.stringLong()
  return "$month $year"
}

@Composable
@ReadOnlyComposable
fun YearMonth.stringShort(): String {
  val month = month.stringShort()
  return "$month $year"
}

@Composable
fun DayOfWeek.stringShort() = stringResource(
  when (this) {
    DayOfWeek.MONDAY -> Res.string.week_mon
    DayOfWeek.TUESDAY -> Res.string.week_tue
    DayOfWeek.WEDNESDAY -> Res.string.week_wed
    DayOfWeek.THURSDAY -> Res.string.week_thu
    DayOfWeek.FRIDAY -> Res.string.week_fri
    DayOfWeek.SATURDAY -> Res.string.week_sat
    DayOfWeek.SUNDAY -> Res.string.week_sun
  }
)

@Composable
@ReadOnlyComposable
fun Month.stringShort() = when (this) {
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
@ReadOnlyComposable
fun Month.stringLong() = when (this) {
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
