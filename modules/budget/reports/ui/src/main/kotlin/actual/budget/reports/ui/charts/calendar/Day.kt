package actual.budget.reports.ui.charts.calendar

import actual.budget.model.Amount
import actual.budget.reports.ui.Action
import actual.budget.reports.vm.CalendarDay
import actual.budget.reports.vm.CalendarMonth
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.ScaleToFitText
import actual.core.ui.Theme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun DayButton(
  day: CalendarDay,
  month: CalendarMonth,
  compact: Boolean,
  onAction: (Action) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Box(
  modifier = modifier
    .background(theme.calendarCellBackground, CardShape)
    .clickable(enabled = day.day >= 1) { onAction(Action.ClickCalendarDay(day)) },
  contentAlignment = Alignment.Center,
) {
  if (day.day < 1) {
    return
  }

  Row(
    modifier = Modifier.fillMaxSize(),
  ) {
    DayBarChart(
      modifier = Modifier
        .weight(1f)
        .fillMaxHeight(),
      dayValue = day.income,
      monthValue = month.income,
      color = theme.reportsBlue,
    )

    DayBarChart(
      modifier = Modifier
        .weight(1f)
        .fillMaxHeight(),
      dayValue = day.expenses,
      monthValue = month.expenses,
      color = theme.reportsRed,
    )
  }

  val padding = if (compact) 4.dp else 8.dp
  ScaleToFitText(
    modifier = Modifier.padding(padding),
    text = day.day.toString(),
    color = theme.tableText,
  )
}

@Composable
internal fun DayBarChart(
  dayValue: Amount,
  monthValue: Amount,
  color: Color,
  modifier: Modifier = Modifier,
) = Column(
  modifier = modifier,
  verticalArrangement = Arrangement.Bottom,
) {
  if (dayValue == Amount.Zero) return

  val fraction = (dayValue / monthValue).coerceIn(0f, 1f)

  if (dayValue != monthValue) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f - fraction)
        .background(color.copy(alpha = 0.2f)),
    )
  }

  val heightModifier = if (dayValue == monthValue) {
    Modifier.fillMaxHeight()
  } else {
    Modifier.weight(fraction)
  }

  Box(
    modifier = heightModifier
      .fillMaxWidth()
      .background(color),
  )
}

@Preview
@Composable
private fun PreviewDayBoth() = PreviewColumn {
  DayButton(
    modifier = Modifier.size(100.dp),
    day = PreviewCalendar.day(day = 26, income = 5345.67, expenses = 1234.56),
    month = PreviewCalendar.JAN_2025.copy(income = Amount(10345.89), expenses = Amount(3456.90)),
    compact = false,
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewDayOnlyIncome() = PreviewColumn {
  DayButton(
    modifier = Modifier.size(100.dp),
    day = PreviewCalendar.day(day = 26, income = 2345.67),
    month = PreviewCalendar.JAN_2025,
    compact = false,
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewDayOnlyExpenses() = PreviewColumn {
  DayButton(
    modifier = Modifier.size(100.dp),
    day = PreviewCalendar.day(day = 26, expenses = 1234.56),
    month = PreviewCalendar.JAN_2025,
    compact = false,
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewDayEmpty() = PreviewColumn {
  DayButton(
    modifier = Modifier.size(100.dp),
    day = PreviewCalendar.day(day = 26),
    month = PreviewCalendar.JAN_2025,
    compact = false,
    onAction = {},
  )
}
