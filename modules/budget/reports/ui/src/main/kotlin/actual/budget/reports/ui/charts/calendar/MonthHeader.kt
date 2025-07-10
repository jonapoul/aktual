package actual.budget.reports.ui.charts.calendar

import actual.budget.model.Amount
import actual.budget.reports.vm.CalendarMonth
import actual.core.icons.ActualIcons
import actual.core.icons.ArrowThickDown
import actual.core.icons.ArrowThickUp
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import actual.core.ui.formattedString
import alakazam.android.ui.compose.HorizontalSpacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun MonthHeader(
  month: CalendarMonth,
  compact: Boolean,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Row(
  modifier = modifier.fillMaxWidth(),
  verticalAlignment = Alignment.CenterVertically,
  horizontalArrangement = Arrangement.End,
) {
  if (!compact) {
    Text(
      modifier = Modifier.weight(1f),
      text = stringLong(month.month),
      fontWeight = FontWeight.Bold,
      color = theme.pageTextSubdued,
    )
  }

  if (compact) {
    Income(
      modifier = Modifier.wrapContentWidth(),
      compact = compact,
      month = month,
      theme = theme,
    )

    HorizontalSpacer(4.dp)

    Expenses(
      modifier = Modifier.wrapContentWidth(),
      compact = compact,
      month = month,
      theme = theme,
    )
  } else {
    Column(
      modifier = Modifier.width(IntrinsicSize.Max),
      horizontalAlignment = Alignment.Start,
      verticalArrangement = Arrangement.Center,
    ) {
      Income(
        modifier = Modifier.fillMaxWidth(),
        compact = compact,
        month = month,
        theme = theme,
      )
      Expenses(
        modifier = Modifier.fillMaxWidth(),
        compact = compact,
        month = month,
        theme = theme,
      )
    }
  }
}

@Composable
private fun Income(
  month: CalendarMonth,
  compact: Boolean,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Row(
  modifier = modifier,
  verticalAlignment = Alignment.CenterVertically,
) {
  Icon(
    modifier = Modifier.size(iconSize(compact)),
    imageVector = ActualIcons.ArrowThickUp,
    tint = theme.reportsBlue,
    contentDescription = null,
  )
  Text(
    modifier = if (compact) Modifier else Modifier.weight(1f),
    text = month.income.formattedString(),
    color = theme.reportsBlue,
    textAlign = TextAlign.End,
    fontSize = fontSize(compact),
  )
}

@Composable
private fun Expenses(
  month: CalendarMonth,
  compact: Boolean,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Row(
  modifier = modifier,
  verticalAlignment = Alignment.CenterVertically,
) {
  Icon(
    modifier = Modifier.size(iconSize(compact)),
    imageVector = ActualIcons.ArrowThickDown,
    tint = theme.reportsRed,
    contentDescription = null,
  )
  Text(
    modifier = if (compact) Modifier else Modifier.weight(1f),
    text = month.expenses.formattedString(),
    color = theme.reportsRed,
    textAlign = TextAlign.End,
    fontSize = fontSize(compact),
  )
}

private fun iconSize(compact: Boolean) = if (compact) 12.dp else 16.dp

private fun fontSize(compact: Boolean) = if (compact) 10.sp else 12.sp

@Preview
@Composable
private fun PreviewMonthHeader() = PreviewColumn {
  MonthHeader(
    month = PreviewCalendar.JAN_2025.copy(expenses = Amount.Zero),
    compact = false,
  )
}

@Preview
@Composable
private fun PreviewMonthHeaderCompact() = PreviewColumn {
  MonthHeader(
    month = PreviewCalendar.JAN_2025,
    compact = true,
  )
}
