/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("ComposeUnstableReceiver")

package aktual.core.ui

import aktual.l10n.Strings
import androidx.compose.runtime.Composable
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

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
fun DayOfWeek.stringShort() = when (this) {
  DayOfWeek.MONDAY -> Strings.weekMon
  DayOfWeek.TUESDAY -> Strings.weekTue
  DayOfWeek.WEDNESDAY -> Strings.weekWed
  DayOfWeek.THURSDAY -> Strings.weekThu
  DayOfWeek.FRIDAY -> Strings.weekFri
  DayOfWeek.SATURDAY -> Strings.weekSat
  DayOfWeek.SUNDAY -> Strings.weekSun
}

@Composable
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
