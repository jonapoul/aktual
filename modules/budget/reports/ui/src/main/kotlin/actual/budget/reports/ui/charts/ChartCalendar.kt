// TODO: Remove UnusedBoxWithConstraintsScope suppression when https://issuetracker.google.com/issues/429780473 is fixed
@file:SuppressLint("UnusedBoxWithConstraintsScope")

package actual.budget.reports.ui.charts

import actual.budget.model.Amount
import actual.budget.reports.ui.Action
import actual.budget.reports.ui.ActionListener
import actual.budget.reports.ui.charts.PreviewShared.HEIGHT
import actual.budget.reports.ui.charts.PreviewShared.WIDTH
import actual.budget.reports.vm.CalendarData
import actual.budget.reports.vm.CalendarDay
import actual.budget.reports.vm.CalendarMonth
import actual.core.icons.ActualIcons
import actual.core.icons.ArrowThickDown
import actual.core.icons.ArrowThickUp
import actual.core.model.ColorSchemeType.Dark
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.PreviewWithColorScheme
import actual.core.ui.ScaleToFitText
import actual.core.ui.Theme
import actual.core.ui.formattedString
import actual.core.ui.scrollbarSettings
import actual.core.ui.stringLong
import actual.core.ui.stringShort
import actual.l10n.Strings
import alakazam.kotlin.compose.HorizontalSpacer
import alakazam.kotlin.compose.VerticalSpacer
import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DayOfWeek
import my.nanihadesuka.compose.LazyRowScrollbar

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
  LazyRowScrollbar(
    state = listState,
    settings = theme.scrollbarSettings(),
  ) {
    LazyRow(
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

@Composable
private fun MonthHeader(
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
    imageVector = ActualIcons.ArrowThickUp,
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
    imageVector = ActualIcons.ArrowThickDown,
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

@Composable
private fun CalendarSummary(
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

@Composable
private fun DayButton(
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

@Preview
@Composable
private fun PreviewDayBoth() = PreviewColumn {
  DayButton(
    modifier = Modifier.size(100.dp),
    day = PreviewCalendar.day(day = 26, income = 5345.67, expenses = 1234.56),
    month = PreviewCalendar.JAN_2025.copy(income = Amount(10345.89), expenses = Amount(3456.90)),
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
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewDayTiny() = PreviewColumn {
  DayButton(
    modifier = Modifier.size(35.dp),
    day = PreviewCalendar.day(day = 26),
    month = PreviewCalendar.JAN_2025,
    onAction = {},
  )
}

@Composable
private fun CalendarMonth(
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

@Preview(heightDp = 900)
@Composable
private fun PreviewMonth() = PreviewColumn {
  CalendarMonth(
    month = PreviewCalendar.JAN_2025,
    compact = false,
    onAction = {},
  )
}

@Preview(heightDp = 900)
@Composable
private fun PreviewMonthWithPrivacy() = PreviewColumn(isPrivacyEnabled = true) {
  CalendarMonth(
    month = PreviewCalendar.JAN_2025,
    compact = false,
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewCompact() = PreviewColumn {
  CalendarMonth(
    modifier = Modifier.height(230.dp),
    month = PreviewCalendar.JAN_2025,
    compact = true,
    onAction = {},
  )
}
