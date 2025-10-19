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
package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.vm.CalendarData
import aktual.budget.reports.vm.CalendarDay
import aktual.budget.reports.vm.CalendarMonth
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

internal object PreviewCalendar {
  val JAN_2025 = CalendarMonth(
    income = Amount(5678.90),
    expenses = Amount(3456.78),
    month = YearMonth(2025, Month.JANUARY),
    days = persistentListOf(
      day(day = -2),
      day(day = -1),
      day(day = 1, income = 301.72),
      day(day = 2, expenses = 25.05),
      day(day = 3, expenses = 152.60),
      day(day = 4),
      day(day = 5, income = 9.99, expenses = 56.63),
      day(day = 6, income = 265, expenses = 227.62),
      day(day = 7, expenses = 10.20),
      day(day = 8, income = 510.52, expenses = 28.1),
      day(day = 9, expenses = 15.2),
      day(day = 10, expenses = 10.2),
      day(day = 11, expenses = 64.78),
      day(day = 12, income = 751.27, expenses = 64.53),
      day(day = 13, income = 80, expenses = 134.85),
      day(day = 14, expenses = 46.66),
      day(day = 15, expenses = 20.4),
      day(day = 16, expenses = 17.85),
      day(day = 17, income = 666.1),
      day(day = 18, income = 714.76, expenses = 745.96),
      day(day = 19, income = 123.45),
      day(day = 20),
      day(day = 21, income = 45.9, expenses = 41.23),
      day(day = 22, expenses = 65.2),
      day(day = 23, expenses = 13.65),
      day(day = 24, income = 589),
      day(day = 25),
      day(day = 26),
      day(day = 27),
      day(day = 28),
      day(day = 29, income = 24, expenses = 24),
      day(day = 30, expenses = 166),
      day(day = 31, income = 2345.67),
    ),
  )

  val ONE_MONTH = CalendarData(
    title = "My Calendar",
    start = YearMonth(2025, Month.JANUARY),
    end = YearMonth(2025, Month.JANUARY),
    income = Amount(5795.88),
    expenses = Amount(3822.78),
    months = persistentListOf(JAN_2025),
  )

  val THREE_MONTHS = CalendarData(
    title = "My Calendar",
    start = YearMonth(2025, Month.JANUARY),
    end = YearMonth(2025, Month.MARCH),
    income = Amount(57958.8),
    expenses = Amount(38227.8),
    months = persistentListOf(
      JAN_2025,
      JAN_2025.copy(month = YearMonth(2025, Month.FEBRUARY)),
      JAN_2025.copy(month = YearMonth(2025, Month.MARCH)),
    ),
  )

  internal fun day(day: Int, income: Number = 0, expenses: Number = 0) = CalendarDay(
    day = day,
    income = Amount(income.toDouble()),
    expenses = Amount(expenses.toDouble()),
  )
}
