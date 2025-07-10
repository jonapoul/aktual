package actual.budget.reports.ui.charts.calendar

import actual.budget.reports.vm.CalendarData
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import actual.core.ui.formattedString
import actual.l10n.Strings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun CalendarSummary(
  data: CalendarData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier.fillMaxWidth(),
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = data.title,
      color = theme.pageText,
      overflow = TextOverflow.Ellipsis,
    )

    val start = stringShort(data.start)
    val end = stringShort(data.end)
    Text(
      text = "$start - $end",
      color = theme.pageTextSubdued,
      overflow = TextOverflow.Ellipsis,
    )
  }

  if (compact) return

  Row {
    Text(
      modifier = Modifier.weight(1f),
      text = Strings.reportsCalendarIncome,
      color = theme.pageText,
      textAlign = TextAlign.End,
    )
    Text(
      modifier = Modifier.weight(1f),
      text = data.income.formattedString(),
      color = theme.reportsBlue,
      textAlign = TextAlign.End,
    )
  }

  Row {
    Text(
      modifier = Modifier.weight(1f),
      text = Strings.reportsCalendarExpenses,
      color = theme.pageText,
      textAlign = TextAlign.End,
    )
    Text(
      modifier = Modifier.weight(1f),
      text = data.expenses.formattedString(),
      color = theme.reportsRed,
      textAlign = TextAlign.End,
    )
  }
}

@Preview
@Composable
private fun PreviewSummaryOneMonth() = PreviewColumn {
  CalendarSummary(
    data = PreviewCalendar.ONE_MONTH,
    compact = false,
  )
}

@Preview
@Composable
private fun PreviewSummaryThreeMonths() = PreviewColumn {
  CalendarSummary(
    data = PreviewCalendar.THREE_MONTHS,
    compact = false,
  )
}

@Preview
@Composable
private fun PreviewSummaryThreeMonthsCompact() = PreviewColumn {
  CalendarSummary(
    data = PreviewCalendar.THREE_MONTHS,
    compact = true,
  )
}
