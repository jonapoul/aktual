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
import aktual.core.l10n.Strings
import aktual.core.ui.CardShape
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ScaleToFitText
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.formattedString
import aktual.core.ui.scrollbar
import aktual.core.ui.stringLong
import aktual.core.ui.stringShort
import alakazam.compose.HorizontalSpacer
import alakazam.compose.VerticalSpacer
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

@Composable
internal fun CalendarChart(
  data: CalendarData,
  compact: Boolean,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  includeHeader: Boolean = true,
) =
  if (compact) {
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
) =
  Column(modifier = modifier) {
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
      items(items = data.months, key = { it.month }) { month ->
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
) =
  Column(modifier = modifier) {
    if (includeHeader) {
      CalendarSummary(
        modifier = Modifier.padding(10.dp).fillMaxWidth().wrapContentHeight(),
        data = data,
        compact = false,
        theme = theme,
      )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      items(items = data.months, key = { it.month }) { month ->
        CalendarMonth(month = month, compact = false, onAction = onAction, theme = theme)
      }
    }
  }

@Composable
internal fun MonthHeader(
  month: CalendarMonth,
  compact: Boolean,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) =
  Row(
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
      Income(modifier = Modifier.wrapContentWidth(), compact = true, month = month, theme = theme)

      HorizontalSpacer(4.dp)

      Expenses(modifier = Modifier.wrapContentWidth(), compact = true, month = month, theme = theme)
    } else {
      Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
      ) {
        Income(modifier = Modifier.fillMaxWidth(), compact = false, month = month, theme = theme)
        Expenses(modifier = Modifier.fillMaxWidth(), compact = false, month = month, theme = theme)
      }
    }
  }

@Composable
private fun Income(
  month: CalendarMonth,
  compact: Boolean,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) =
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
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
) =
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
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
): Unit =
  Column(modifier = modifier.fillMaxWidth()) {
    Row(modifier = Modifier.fillMaxWidth()) {
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

@Suppress("UnusedBoxWithConstraintsScope")
@Composable
internal fun DayButton(
  day: CalendarDay,
  month: CalendarMonth,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  if (!day.isValid) return

  BoxWithConstraints(
    modifier =
      modifier
        .background(
          if (!day.isValid) Color.Transparent else theme.calendarCellBackground,
          CardShape,
        )
        .clickable(enabled = day.isValid) { onAction(Action.ClickCalendarDay(day)) },
    contentAlignment = Alignment.Center,
  ) {
    Row(modifier = Modifier.fillMaxSize()) {
      DayBarChart(
        modifier = Modifier.weight(1f).fillMaxHeight(),
        dayValue = day.income,
        monthValue = month.income,
        color = theme.reportsBlue,
      )

      DayBarChart(
        modifier = Modifier.weight(1f).fillMaxHeight(),
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
}

@get:Stable
private val CalendarDay.isValid
  get() = day >= 1

@Composable
private fun DayBarChart(
  dayValue: Amount,
  monthValue: Amount,
  color: Color,
  modifier: Modifier = Modifier,
): Unit =
  Column(modifier = modifier, verticalArrangement = Arrangement.Bottom) {
    if (dayValue == Amount.Zero) return

    val fraction = (dayValue / monthValue).coerceIn(0f, 1f)

    if (dayValue != monthValue) {
      Box(
        modifier =
          Modifier.fillMaxWidth().weight(1f - fraction).background(color.copy(alpha = 0.2f))
      )
    }

    val heightModifier =
      if (dayValue == monthValue) {
        Modifier.fillMaxHeight()
      } else {
        Modifier.weight(fraction)
      }

    Box(modifier = heightModifier.fillMaxWidth().background(color))
  }

@Composable
internal fun CalendarMonth(
  month: CalendarMonth,
  compact: Boolean,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  minSize: Dp = 300.dp,
) =
  Column(
    modifier =
      modifier
        .background(theme.tableBackground, CardShape)
        .widthIn(min = minSize)
        .heightIn(min = minSize)
        .padding(10.dp)
  ) {
    MonthHeader(
      modifier = Modifier.fillMaxWidth().wrapContentHeight(),
      month = month,
      compact = compact,
      theme = theme,
    )

    VerticalSpacer(5.dp)

    Row(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
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

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
      val weeks = remember(month) { month.toWeeks() }
      val itemHeight = maxHeight / weeks.size - TABLE_SPACING
      val itemWidth = maxWidth / DAYS_PER_WEEK - TABLE_SPACING

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

@Preview
@Composable
private fun PreviewCalendarChart(
  @PreviewParameter(CalendarChartProvider::class) params: ThemedParams<CalendarChartParams>
) =
  PreviewWithColorScheme(params.type) {
    CalendarChart(
      modifier =
        Modifier.background(LocalTheme.current.tableBackground, CardShape)
          .width(WIDTH.dp)
          .height(HEIGHT.dp)
          .padding(5.dp),
      compact = params.data.compact,
      data = params.data.data,
      onAction = {},
    )
  }

private data class CalendarChartParams(val data: CalendarData, val compact: Boolean)

private class CalendarChartProvider :
  ThemedParameterProvider<CalendarChartParams>(
    CalendarChartParams(ONE_MONTH, compact = true),
    CalendarChartParams(THREE_MONTHS, compact = true),
    CalendarChartParams(ONE_MONTH, compact = false),
    CalendarChartParams(THREE_MONTHS, compact = false),
  )

@Preview
@Composable
private fun PreviewMonthHeader(
  @PreviewParameter(MonthHeaderProvider::class) params: ThemedParams<MonthHeaderParams>
) =
  PreviewWithColorScheme(params.type) {
    MonthHeader(month = params.data.month, compact = params.data.compact)
  }

private data class MonthHeaderParams(val month: CalendarMonth, val compact: Boolean)

private class MonthHeaderProvider :
  ThemedParameterProvider<MonthHeaderParams>(
    MonthHeaderParams(JAN_2025.copy(expenses = Amount.Zero), compact = false),
    MonthHeaderParams(JAN_2025, compact = true),
  )

@Preview
@Composable
private fun PreviewCalendarSummary(
  @PreviewParameter(CalendarSummaryProvider::class) params: ThemedParams<CalendarSummaryParams>
) =
  PreviewWithColorScheme(params.type) {
    CalendarSummary(data = params.data.data, compact = params.data.compact)
  }

private data class CalendarSummaryParams(val data: CalendarData, val compact: Boolean)

private class CalendarSummaryProvider :
  ThemedParameterProvider<CalendarSummaryParams>(
    CalendarSummaryParams(ONE_MONTH, compact = false),
    CalendarSummaryParams(THREE_MONTHS, compact = false),
    CalendarSummaryParams(THREE_MONTHS, compact = true),
  )

@Preview
@Composable
private fun PreviewDayButton(
  @PreviewParameter(DayButtonProvider::class) params: ThemedParams<DayButtonParams>
) =
  PreviewWithColorScheme(params.type) {
    DayButton(
      modifier = Modifier.size(params.data.size),
      day = params.data.day,
      month = params.data.month,
      onAction = {},
    )
  }

private data class DayButtonParams(
  val day: CalendarDay,
  val month: CalendarMonth,
  val size: Dp = 100.dp,
)

private class DayButtonProvider :
  ThemedParameterProvider<DayButtonParams>(
    DayButtonParams(
      day = day(day = 26, income = 5345.67, expenses = 1234.56),
      month = JAN_2025.copy(income = Amount(value = 10345.89), expenses = Amount(value = 3456.90)),
    ),
    DayButtonParams(day = day(day = 26, income = 2345.67), month = JAN_2025),
    DayButtonParams(day = day(day = 26, expenses = 1234.56), month = JAN_2025),
    DayButtonParams(day = day(day = 26), month = JAN_2025),
    DayButtonParams(day = day(day = 26), month = JAN_2025, size = 35.dp),
  )

@Preview
@Composable
private fun PreviewCalendarMonth(
  @PreviewParameter(CalendarMonthProvider::class) params: ThemedParams<CalendarMonthParams>
) =
  PreviewWithColorScheme(
    schemeType = params.type,
    isPrivacyEnabled = params.data.isPrivacyEnabled,
  ) {
    CalendarMonth(month = params.data.month, compact = params.data.compact, onAction = {})
  }

private data class CalendarMonthParams(
  val month: CalendarMonth,
  val compact: Boolean,
  val isPrivacyEnabled: Boolean = false,
)

private class CalendarMonthProvider :
  ThemedParameterProvider<CalendarMonthParams>(
    CalendarMonthParams(month = JAN_2025, compact = false),
    CalendarMonthParams(month = JAN_2025, compact = false, isPrivacyEnabled = true),
    CalendarMonthParams(month = JAN_2025, compact = true),
  )

private val JAN_2025 =
  CalendarMonth(
    income = Amount(5678.90),
    expenses = Amount(3456.78),
    month = YearMonth(2025, Month.JANUARY),
    days =
      persistentListOf(
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

private val ONE_MONTH =
  CalendarData(
    title = "My Calendar",
    start = YearMonth(2025, Month.JANUARY),
    end = YearMonth(2025, Month.JANUARY),
    income = Amount(5795.88),
    expenses = Amount(3822.78),
    months = persistentListOf(JAN_2025),
  )

internal val THREE_MONTHS =
  CalendarData(
    title = "My Calendar",
    start = YearMonth(2025, Month.JANUARY),
    end = YearMonth(2025, Month.MARCH),
    income = Amount(57958.8),
    expenses = Amount(38227.8),
    months =
      persistentListOf(
        JAN_2025,
        JAN_2025.copy(month = YearMonth(2025, Month.FEBRUARY)),
        JAN_2025.copy(month = YearMonth(2025, Month.MARCH)),
      ),
  )

private fun day(day: Int, income: Number = 0, expenses: Number = 0) =
  CalendarDay(day = day, income = Amount(income.toDouble()), expenses = Amount(expenses.toDouble()))
