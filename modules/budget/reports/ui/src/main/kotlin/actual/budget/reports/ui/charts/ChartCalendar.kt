package actual.budget.reports.ui.charts

import actual.budget.reports.ui.Action
import actual.budget.reports.ui.charts.PreviewShared.HEIGHT
import actual.budget.reports.ui.charts.PreviewShared.WIDTH
import actual.budget.reports.ui.charts.calendar.CalendarSummary
import actual.budget.reports.ui.charts.calendar.PreviewCalendar
import actual.budget.reports.ui.charts.calendar.CalendarMonth
import actual.budget.reports.vm.CalendarData
import actual.core.model.ColorSchemeType.Dark
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewWithColorScheme
import actual.core.ui.Theme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun CalendarChart(
  data: CalendarData,
  compact: Boolean,
  onAction: (Action) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = if (compact) {
  CompactCalendarChart(data, onAction, modifier, theme)
} else {
  RegularCalendarChart(data, onAction, modifier, theme)
}

@Composable
private fun CompactCalendarChart(
  data: CalendarData,
  onAction: (Action) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier,
) {
  CalendarSummary(
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight(),
    data = data,
    compact = true,
    theme = theme,
  )

  LazyRow(
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    items(items = data.months, key = { it.month }) { month ->
      CalendarMonth(
        month = month,
        compact = true,
        onAction = onAction,
        theme = theme,
      )
    }
  }
}
@Composable
private fun RegularCalendarChart(
  data: CalendarData,
  onAction: (Action) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier,
) {
  CalendarSummary(
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight(),
    data = data,
    compact = false,
    theme = theme,
  )

  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    items(items = data.months, key = { it.month }) { month ->
      CalendarMonth(
        month = month,
        compact = false,
        onAction = onAction,
        theme = theme,
      )
    }
  }
}

@Preview(widthDp = WIDTH, heightDp = HEIGHT)
@Composable
private fun PreviewCompactOneMonth() = PreviewChart(PreviewCalendar.ONE_MONTH, true)

@Preview(widthDp = WIDTH, heightDp = HEIGHT)
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
