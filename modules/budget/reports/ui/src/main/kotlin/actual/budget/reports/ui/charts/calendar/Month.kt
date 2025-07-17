// TODO: Remove UnusedBoxWithConstraintsScope suppression when https://issuetracker.google.com/issues/429780473 is fixed
@file:SuppressLint("UnusedBoxWithConstraintsScope")

package actual.budget.reports.ui.charts.calendar

import actual.budget.reports.ui.Action
import actual.budget.reports.vm.CalendarDay
import actual.budget.reports.vm.CalendarMonth
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import actual.core.ui.stringShort
import alakazam.android.ui.compose.VerticalSpacer
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DayOfWeek

@Composable
internal fun CalendarMonth(
  month: CalendarMonth,
  compact: Boolean,
  onAction: (Action) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier
    .background(theme.tableBackground, CardShape)
    .widthIn(min = 300.dp)
    .heightIn(min = 300.dp)
    .padding(10.dp),
) {
  MonthHeader(
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight(),
    month = month,
    compact = compact,
    theme = theme,
  )

  VerticalSpacer(5.dp)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight(),
  ) {
    DayOfWeek.entries.fastForEach { day ->
      Text(
        modifier = Modifier.weight(1f),
        text = day.stringShort(),
        textAlign = TextAlign.Center,
        color = theme.pageTextSubdued,
      )
    }
  }

  VerticalSpacer(5.dp)

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxWidth()
      .weight(1f),
  ) {
    val weeks = remember(month) { month.toWeeks() }
    val itemHeight = (maxHeight / weeks.size) - TABLE_SPACING
    val itemWidth = (maxWidth / DAYS_PER_WEEK) - TABLE_SPACING

    Column(verticalArrangement = Arrangement.spacedBy(TABLE_SPACING)) {
      weeks.fastForEach { week ->
        Row(horizontalArrangement = Arrangement.spacedBy(TABLE_SPACING)) {
          week.fastForEach { day ->
            DayButton(
              modifier = Modifier.size(width = itemWidth, height = itemHeight),
              day = day,
              month = month,
              compact = compact,
              onAction = onAction,
              theme = theme,
            )
          }
        }
      }
    }
  }
}

private fun CalendarMonth.toWeeks(): ImmutableList<ImmutableList<CalendarDay>> {
  val weeks = mutableListOf<ImmutableList<CalendarDay>>()
  var currentWeek = mutableListOf<CalendarDay>()
  for (day in days) {
    currentWeek.add(day)
    if (currentWeek.size == DAYS_PER_WEEK) {
      weeks.add(currentWeek.toImmutableList())
      currentWeek = mutableListOf()
    }
  }
  if (currentWeek.isNotEmpty()) {
    weeks.add(currentWeek.toImmutableList())
  }
  return weeks.toImmutableList()
}

private const val DAYS_PER_WEEK = 7
private val TABLE_SPACING = 2.dp

@Preview(heightDp = 900)
@Composable
private fun PreviewMonth() = PreviewColumn {
  CalendarMonth(
    month = PreviewCalendar.JAN_2025,
    compact = false,
    onAction = {},
  )
}
