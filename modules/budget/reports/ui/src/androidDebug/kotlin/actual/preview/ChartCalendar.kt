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
package actual.preview

import actual.budget.model.Amount
import actual.budget.reports.ui.charts.CalendarChart
import actual.budget.reports.ui.charts.CalendarMonth
import actual.budget.reports.ui.charts.CalendarSummary
import actual.budget.reports.ui.charts.DayButton
import actual.budget.reports.ui.charts.MonthHeader
import actual.budget.reports.ui.charts.PreviewCalendar
import actual.budget.reports.ui.charts.PreviewShared.HEIGHT
import actual.budget.reports.ui.charts.PreviewShared.WIDTH
import actual.budget.reports.vm.CalendarData
import actual.core.model.ColorSchemeType.Dark
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewThemedColumn
import actual.core.ui.PreviewWithColorScheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(widthDp = WIDTH, heightDp = 400)
@Composable
private fun PreviewCompactOneMonth() = PreviewChart(PreviewCalendar.ONE_MONTH, true)

@Preview(widthDp = WIDTH, heightDp = 400)
@Composable
private fun PreviewCompactThreeMonth() = PreviewChart(PreviewCalendar.THREE_MONTHS, true)

@Preview(widthDp = WIDTH, heightDp = HEIGHT * 5)
@Composable
private fun PreviewRegularOneMonth() = PreviewChart(PreviewCalendar.ONE_MONTH, false)

@Preview(widthDp = WIDTH, heightDp = HEIGHT * 5)
@Composable
private fun PreviewRegularThreeMonth() = PreviewChart(PreviewCalendar.THREE_MONTHS, false)

@Composable
private fun PreviewChart(data: CalendarData, compact: Boolean) = PreviewWithColorScheme(Dark) {
  CalendarChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp)
      .height(HEIGHT.dp)
      .padding(5.dp),
    compact = compact,
    data = data,
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewMonthHeader() = PreviewThemedColumn {
  MonthHeader(
    month = PreviewCalendar.JAN_2025.copy(expenses = Amount.Zero),
    compact = false,
  )
}

@Preview
@Composable
private fun PreviewMonthHeaderCompact() = PreviewThemedColumn {
  MonthHeader(
    month = PreviewCalendar.JAN_2025,
    compact = true,
  )
}

@Preview
@Composable
private fun PreviewSummaryOneMonth() = PreviewThemedColumn {
  CalendarSummary(
    data = PreviewCalendar.ONE_MONTH,
    compact = false,
  )
}

@Preview
@Composable
private fun PreviewSummaryThreeMonths() = PreviewThemedColumn {
  CalendarSummary(
    data = PreviewCalendar.THREE_MONTHS,
    compact = false,
  )
}

@Preview
@Composable
private fun PreviewSummaryThreeMonthsCompact() = PreviewThemedColumn {
  CalendarSummary(
    data = PreviewCalendar.THREE_MONTHS,
    compact = true,
  )
}

@Preview
@Composable
private fun PreviewDayBoth() = PreviewThemedColumn {
  DayButton(
    modifier = Modifier.size(100.dp),
    day = PreviewCalendar.day(day = 26, income = 5345.67, expenses = 1234.56),
    month = PreviewCalendar.JAN_2025.copy(income = Amount(10345.89), expenses = Amount(3456.90)),
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewDayOnlyIncome() = PreviewThemedColumn {
  DayButton(
    modifier = Modifier.size(100.dp),
    day = PreviewCalendar.day(day = 26, income = 2345.67),
    month = PreviewCalendar.JAN_2025,
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewDayOnlyExpenses() = PreviewThemedColumn {
  DayButton(
    modifier = Modifier.size(100.dp),
    day = PreviewCalendar.day(day = 26, expenses = 1234.56),
    month = PreviewCalendar.JAN_2025,
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewDayEmpty() = PreviewThemedColumn {
  DayButton(
    modifier = Modifier.size(100.dp),
    day = PreviewCalendar.day(day = 26),
    month = PreviewCalendar.JAN_2025,
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewDayTiny() = PreviewThemedColumn {
  DayButton(
    modifier = Modifier.size(35.dp),
    day = PreviewCalendar.day(day = 26),
    month = PreviewCalendar.JAN_2025,
    onAction = {},
  )
}

@Preview(heightDp = 900)
@Composable
private fun PreviewMonth() = PreviewThemedColumn {
  CalendarMonth(
    month = PreviewCalendar.JAN_2025,
    compact = false,
    onAction = {},
  )
}

@Preview(heightDp = 900)
@Composable
private fun PreviewMonthWithPrivacy() = PreviewThemedColumn(isPrivacyEnabled = true) {
  CalendarMonth(
    month = PreviewCalendar.JAN_2025,
    compact = false,
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewCompact() = PreviewThemedColumn {
  CalendarMonth(
    modifier = Modifier.height(230.dp),
    month = PreviewCalendar.JAN_2025,
    compact = true,
    onAction = {},
  )
}
