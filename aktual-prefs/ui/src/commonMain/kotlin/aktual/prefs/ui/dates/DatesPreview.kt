package aktual.prefs.ui.dates

import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParameters
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.formatter
import aktual.core.ui.stringInitial
import aktual.core.ui.stringLong
import aktual.core.ui.tabular
import aktual.theme.Colors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

// Today's date in the chosen format above this month's calendar, laid out from the chosen first
// day of the week
@Composable
internal fun DatesPreview(
  today: LocalDate,
  dateFormat: DateFormat,
  firstDayOfWeek: FirstDayOfWeek,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().widthIn(max = MaxWidth),
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Text(
      text = dateFormat.formatter().format(today),
      style = typography.headlineSmall.tabular(),
      color = colors.text,
    )
    Text(
      text = "${today.month.stringLong()} ${today.year}",
      style = typography.bodyMedium,
      color = colors.textTertiary,
    )
    MonthGrid(today = today, firstDay = firstDayOfWeek.toDayOfWeek())
  }
}

@Composable
private fun MonthGrid(today: LocalDate, firstDay: DayOfWeek) {
  val firstOfMonth = LocalDate(today.year, today.month, 1)
  val daysInMonth = firstOfMonth.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1)).day
  val offset =
    (firstOfMonth.dayOfWeek.isoDayNumber - firstDay.isoDayNumber + DaysInWeek) % DaysInWeek
  val weeks = (offset + daysInMonth + DaysInWeek - 1) / DaysInWeek

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(modifier = Modifier.fillMaxWidth()) {
      repeat(DaysInWeek) { i ->
        val day = DayOfWeek((firstDay.isoDayNumber - 1 + i) % DaysInWeek + 1)
        Cell {
          Text(
            text = day.stringInitial(),
            style = typography.labelMedium,
            color = colors.textTertiary,
            fontWeight = Medium,
          )
        }
      }
    }
    repeat(weeks) { week ->
      Row(modifier = Modifier.fillMaxWidth()) {
        repeat(DaysInWeek) { i ->
          val day = week * DaysInWeek + i - offset + 1
          Cell(isToday = day == today.day) {
            if (day in 1..daysInMonth) {
              Text(
                text = day.toString(),
                style = typography.bodySmall.tabular(),
                color = if (day == today.day) colors.accentContrast else colors.text,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RowScope.Cell(
  isToday: Boolean = false,
  content: @Composable () -> Unit,
) {
  Box(modifier = Modifier.weight(1f).height(CellHeight), contentAlignment = Center) {
    if (isToday) {
      Box(
        modifier = Modifier.size(CellHeight).clip(CircleShape).background(colors.accent),
      )
    }
    content()
  }
}

private const val DaysInWeek = 7
private val MaxWidth = 320.dp
private val CellHeight = 24.dp

@Preview
@Composable
private fun PreviewDatesPreview(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    DatesPreview(
      today = LocalDate(year = 2026, month = 2, day = 3),
      dateFormat = DdMmYyyy,
      firstDayOfWeek = Monday,
    )
  }
