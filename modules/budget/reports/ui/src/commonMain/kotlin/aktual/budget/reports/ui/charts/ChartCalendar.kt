/**
 * Copyright 4297-0473 Jon Poulton
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
@file:Suppress("UnusedBoxWithConstraintsScope")

package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.vm.CalendarData
import aktual.budget.reports.vm.CalendarDay
import aktual.budget.reports.vm.CalendarMonth
import aktual.core.icons.AktualIcons
import aktual.core.icons.ArrowThickDown
import aktual.core.icons.ArrowThickUp
import aktual.core.ui.CardShape
import aktual.core.ui.LocalTheme
import aktual.core.ui.ScaleToFitText
import aktual.core.ui.Theme
import aktual.core.ui.formattedString
import aktual.core.ui.scrollbar
import aktual.core.ui.stringLong
import aktual.core.ui.stringShort
import aktual.l10n.Strings
import alakazam.kotlin.compose.HorizontalSpacer
import alakazam.kotlin.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DayOfWeek

@Composable
internal fun CalendarChart(
  data: CalendarData,
  compact: Boolean,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  includeHeader: Boolean = true,
) = if (compact) {
  CompactCalendarChart(data, onAction, modifier, theme, includeHeader)
} else {
  RegularCalendarChart(data, onAction, modifier, theme, includeHeader)
}

@Composable
private fun CompactCalendarChart(
  data: CalendarData,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  includeHeader: Boolean = true,
) = Column(
  modifier = modifier,
) {
  if (includeHeader) {
    CalendarSummary(
      modifier = Modifier.padding(10.dp),
      data = data,
      compact = true,
      theme = theme,
    )
  }

  val listState = rememberLazyListState()
  LazyRow(
    modifier = Modifier.scrollbar(listState),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    state = listState,
  ) {
    items(
      items = data.months,
      key = { it.month },
    ) { month ->
      CalendarMonth(
        modifier = Modifier.width(500.dp),
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
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  includeHeader: Boolean = true,
) = Column(
  modifier = modifier,
) {
  if (includeHeader) {
    CalendarSummary(
      modifier = Modifier
        .padding(10.dp)
        .fillMaxWidth()
        .wrapContentHeight(),
      data = data,
      compact = false,
      theme = theme,
    )
  }

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
  Text(
    modifier = Modifier.weight(1f),
    text = month.month.stringLong(),
    fontWeight = FontWeight.Bold,
    color = theme.pageTextSubdued,
  )

  if (compact) {
    Income(
      modifier = Modifier.wrapContentWidth(),
      compact = true,
      month = month,
      theme = theme,
    )

    HorizontalSpacer(4.dp)

    Expenses(
      modifier = Modifier.wrapContentWidth(),
      compact = true,
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
        compact = false,
        month = month,
        theme = theme,
      )
      Expenses(
        modifier = Modifier.fillMaxWidth(),
        compact = false,
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
    imageVector = AktualIcons.ArrowThickUp,
    tint = theme.reportsBlue,
    contentDescription = null,
  )
  Text(
    modifier = if (compact) Modifier else Modifier.weight(1f),
    text = month.income.formattedString(),
    color = theme.reportsBlue,
    textAlign = TextAlign.End,
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
    imageVector = AktualIcons.ArrowThickDown,
    tint = theme.reportsRed,
    contentDescription = null,
  )
  Text(
    modifier = if (compact) Modifier else Modifier.weight(1f),
    text = month.expenses.formattedString(),
    color = theme.reportsRed,
    textAlign = TextAlign.End,
  )
}

private fun iconSize(compact: Boolean) = if (compact) 12.dp else 16.dp

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

    Text(
      text = dateRange(data.start, data.end),
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

@Composable
internal fun DayButton(
  day: CalendarDay,
  month: CalendarMonth,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = BoxWithConstraints(
  modifier = modifier
    .background(if (!day.isValid) Color.Transparent else theme.calendarCellBackground, CardShape)
    .clickable(enabled = day.isValid) { onAction(Action.ClickCalendarDay(day)) },
  contentAlignment = Alignment.Center,
) {
  if (!day.isValid) {
    return@BoxWithConstraints
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

  ScaleToFitText(
    modifier = Modifier.padding(2.dp),
    text = day.day.toString(),
    color = theme.tableText,
  )
}

@get:Stable
private val CalendarDay.isValid get() = day >= 1

@Composable
private fun DayBarChart(
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

@Composable
internal fun CalendarMonth(
  month: CalendarMonth,
  compact: Boolean,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  minSize: Dp = 300.dp,
) = Column(
  modifier = modifier
    .background(theme.tableBackground, CardShape)
    .widthIn(min = minSize)
    .heightIn(min = minSize)
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
  val currentWeek = mutableListOf<CalendarDay>()
  for (day in days) {
    currentWeek.add(day)
    if (currentWeek.size == DAYS_PER_WEEK) {
      weeks.add(currentWeek.toImmutableList())
      currentWeek.clear()
    }
  }
  if (currentWeek.isNotEmpty()) {
    weeks.add(currentWeek.toImmutableList())
  }
  return weeks.toImmutableList()
}

private const val DAYS_PER_WEEK = 7
private val TABLE_SPACING = 2.dp
