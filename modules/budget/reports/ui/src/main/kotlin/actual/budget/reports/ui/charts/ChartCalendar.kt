package actual.budget.reports.ui.charts

import actual.budget.model.Amount
import actual.budget.reports.ui.Action
import actual.budget.reports.ui.charts.PreviewShared.WIDTH
import actual.budget.reports.vm.CalendarData
import actual.budget.reports.vm.CalendarDay
import actual.budget.reports.vm.CalendarMonth
import actual.core.icons.ActualIcons
import actual.core.icons.ArrowThickDown
import actual.core.icons.ArrowThickUp
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.ScaleToFitText
import actual.core.ui.Theme
import actual.core.ui.formattedString
import actual.l10n.Strings
import alakazam.android.ui.compose.VerticalSpacer
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
internal fun CalendarChart(
  data: CalendarData,
  compact: Boolean,
  onAction: (Action) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = if (compact) {
  CompactCalendar(data, onAction, modifier, theme)
} else {
  RegularCalendar(data, onAction, modifier, theme)
}

@Composable
private fun CompactCalendar(
  data: CalendarData,
  onAction: (Action) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {

}

@Composable
private fun RegularCalendar(
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
    theme = theme,
  )

  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    items(
      items = data.months,
      key = { it.month },
    ) { month ->
      RegularCalendarMonth(
        modifier = Modifier.height(300.dp),
        month = month,
        onAction = onAction,
        theme = theme,
      )
    }
  }
}

// TODO: Remove UnusedBoxWithConstraintsScope suppression when https://issuetracker.google.com/issues/429780473 is fixed
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun RegularCalendarMonth(
  month: CalendarMonth,
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
        text = stringShort(day),
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
    val numWeeks = ceil(month.days.size / DAYS_PER_WEEK.toDouble()).roundToInt()
    val itemHeight = (maxHeight / numWeeks) - TABLE_SPACING

    LazyVerticalGrid(
      columns = GridCells.Fixed(DAYS_PER_WEEK),
      verticalArrangement = Arrangement.spacedBy(TABLE_SPACING),
      horizontalArrangement = Arrangement.spacedBy(TABLE_SPACING),
    ) {
      items(
        items = month.days,
        key = { it.day },
      ) { day ->
        DayButton(
          modifier = Modifier.height(itemHeight),
          day = day,
          onAction = onAction,
          theme = theme,
        )
      }
    }
  }
}

@Composable
private fun DayButton(
  day: CalendarDay,
  onAction: (Action) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  if (day.day < 1) return

  Box(
    modifier = modifier
      .background(theme.calendarCellBackground, CardShape)
      .clickable { onAction(Action.ClickCalendarDay(day)) },
    contentAlignment = Alignment.Center,
  ) {
    ScaleToFitText(
      modifier = Modifier.padding(8.dp),
      text = day.day.toString(),
      color = theme.tableText,
    )
  }
}

@Composable
private fun CalendarSummary(
  data: CalendarData,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier.fillMaxWidth(),
) {
  val start = stringShort(data.start)
  val end = stringShort(data.end)
  Text(
    text = "$start - $end",
    color = theme.pageTextSubdued,
  )

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
private fun MonthHeader(
  month: CalendarMonth,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Row(
  modifier = modifier.fillMaxWidth(),
  verticalAlignment = Alignment.CenterVertically,
) {
  Text(
    modifier = Modifier.weight(1f),
    text = stringLong(month.month),
    fontWeight = FontWeight.Bold,
    color = theme.pageTextSubdued,
  )

  Column(
    modifier = Modifier.width(IntrinsicSize.Max),
    horizontalAlignment = Alignment.Start,
    verticalArrangement = Arrangement.Center,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        modifier = Modifier.size(16.dp),
        imageVector = ActualIcons.ArrowThickUp,
        tint = theme.reportsBlue,
        contentDescription = null,
      )
      Text(
        modifier = Modifier.weight(1f),
        text = month.income.formattedString(),
        color = theme.reportsBlue,
        textAlign = TextAlign.End,
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        modifier = Modifier.size(16.dp),
        imageVector = ActualIcons.ArrowThickDown,
        tint = theme.reportsRed,
        contentDescription = null,
      )
      Text(
        modifier = Modifier.weight(1f),
        text = month.expenses.formattedString(),
        color = theme.reportsRed,
        textAlign = TextAlign.End,
      )
    }
  }
}

@Composable
@ReadOnlyComposable
private fun stringLong(yearMonth: YearMonth): String {
  val month = when (yearMonth.month) {
    Month.JANUARY -> Strings.reportsMonthJanL
    Month.FEBRUARY -> Strings.reportsMonthFebL
    Month.MARCH -> Strings.reportsMonthMarL
    Month.APRIL -> Strings.reportsMonthAprL
    Month.MAY -> Strings.reportsMonthMayL
    Month.JUNE -> Strings.reportsMonthJunL
    Month.JULY -> Strings.reportsMonthJulL
    Month.AUGUST -> Strings.reportsMonthAugL
    Month.SEPTEMBER -> Strings.reportsMonthSepL
    Month.OCTOBER -> Strings.reportsMonthOctL
    Month.NOVEMBER -> Strings.reportsMonthNovL
    Month.DECEMBER -> Strings.reportsMonthDecL
  }
  return "$month ${yearMonth.year}"
}

@Composable
@ReadOnlyComposable
private fun stringShort(yearMonth: YearMonth): String {
  val month = when (yearMonth.month) {
    Month.JANUARY -> Strings.reportsMonthJan
    Month.FEBRUARY -> Strings.reportsMonthFeb
    Month.MARCH -> Strings.reportsMonthMar
    Month.APRIL -> Strings.reportsMonthApr
    Month.MAY -> Strings.reportsMonthMay
    Month.JUNE -> Strings.reportsMonthJun
    Month.JULY -> Strings.reportsMonthJul
    Month.AUGUST -> Strings.reportsMonthAug
    Month.SEPTEMBER -> Strings.reportsMonthSep
    Month.OCTOBER -> Strings.reportsMonthOct
    Month.NOVEMBER -> Strings.reportsMonthNov
    Month.DECEMBER -> Strings.reportsMonthDec
  }
  return "$month ${yearMonth.year}"
}

@Composable
@ReadOnlyComposable
private fun stringShort(day: DayOfWeek) = when (day) {
  DayOfWeek.MONDAY -> Strings.reportsWeekMon
  DayOfWeek.TUESDAY -> Strings.reportsWeekTue
  DayOfWeek.WEDNESDAY -> Strings.reportsWeekWed
  DayOfWeek.THURSDAY -> Strings.reportsWeekThu
  DayOfWeek.FRIDAY -> Strings.reportsWeekFri
  DayOfWeek.SATURDAY -> Strings.reportsWeekSat
  DayOfWeek.SUNDAY -> Strings.reportsWeekSun
}

private suspend fun CartesianChartModelProducer.populate(
  day: CalendarDay,
  month: CalendarMonth,
) = runTransaction {
  columnSeries {
    if (day.income == Amount.Zero) {
      series(0.0)
    } else {
      series(day.income.toDouble())
      series((month.income - day.income).toDouble())
    }
  }

  columnSeries {
    if (day.expenses == Amount.Zero) {
      series(0.0)
    } else {
      series(day.expenses.toDouble())
      series((month.expenses - day.expenses).toDouble())
    }
  }
}

private const val DAYS_PER_WEEK = 7

private val TABLE_SPACING = 2.dp

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewCompactOneMonth() = PreviewChart(PreviewCalendar.ONE_MONTH, true)

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewRegularOneMonth() = PreviewChart(PreviewCalendar.ONE_MONTH, false)

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewCompactThreeMonth() = PreviewChart(PreviewCalendar.THREE_MONTHS, true)

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewRegularThreeMonth() = PreviewChart(PreviewCalendar.THREE_MONTHS, false)

@Composable
private fun PreviewChart(data: CalendarData, compact: Boolean) = PreviewColumn {
  CalendarChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp)
      .padding(5.dp),
    compact = compact,
    data = data,
    onAction = {},
  )
}

@Preview(heightDp = 900)
@Composable
private fun PreviewMonth() = PreviewColumn {
  RegularCalendarMonth(month = PreviewCalendar.JAN_2025, onAction = {})
}
