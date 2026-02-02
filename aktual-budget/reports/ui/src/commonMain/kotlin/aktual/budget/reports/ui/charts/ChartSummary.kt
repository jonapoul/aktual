package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.ui.charts.AMOUNT
import aktual.budget.reports.ui.charts.END_DATE
import aktual.budget.reports.ui.charts.START_DATE
import aktual.budget.reports.ui.charts.WIDTH
import aktual.budget.reports.vm.DateRange
import aktual.budget.reports.vm.PercentageDivisor
import aktual.budget.reports.vm.SummaryChartType
import aktual.budget.reports.vm.SummaryData
import aktual.core.icons.AktualIcons
import aktual.core.icons.CloseBracket
import aktual.core.icons.Equals
import aktual.core.icons.OpenBracket
import aktual.core.icons.Sum
import aktual.core.l10n.Strings
import aktual.core.model.Percent
import aktual.core.model.percent
import aktual.core.ui.AktualTypography
import aktual.core.ui.CardShape
import aktual.core.ui.ExposedDropDownMenu
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ScaleToFitText
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.formattedString
import aktual.core.ui.stringShort
import alakazam.kotlin.compose.HorizontalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.yearMonth

@Composable
internal fun SummaryChart(
  data: SummaryData,
  compact: Boolean,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  includeHeader: Boolean = true,
) = Column(
  modifier = modifier,
  horizontalAlignment = Alignment.CenterHorizontally,
  verticalArrangement = Arrangement.Center,
) {
  if (compact && includeHeader) {
    Header(
      modifier = Modifier.fillMaxWidth(),
      data = data,
      theme = theme,
    )
  }

  when (data) {
    is SummaryData.AveragePerMonth -> if (compact) {
      CompactAmount(data.average)
    } else {
      RegularPerMonth(data, onAction)
    }

    is SummaryData.AveragePerTransaction -> if (compact) {
      CompactAmount(data.average)
    } else {
      RegularPerTransaction(data, onAction)
    }

    is SummaryData.Percentage -> if (compact) {
      CompactPercent(data.percent)
    } else {
      RegularPercent(data, onAction)
    }

    is SummaryData.Sum -> if (compact) {
      CompactAmount(data.value)
    } else {
      RegularSum(data, onAction)
    }
  }
}

@Composable
private fun Header(
  data: SummaryData,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier,
  horizontalAlignment = Alignment.Start,
) {
  Text(
    text = data.title,
    color = theme.pageText,
    style = AktualTypography.bodyLarge,
  )

  data.start?.yearMonth?.let { start ->
    data.end?.yearMonth?.let { end ->
      Text(
        text = dateRange(start, end),
        color = theme.pageTextSubdued,
        style = AktualTypography.bodyMedium,
      )
    }
  }
}

@Composable
private fun CompactAmount(
  amount: Amount,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = ScaleToFitText(
  modifier = modifier.padding(16.dp),
  color = if (amount.isPositive()) theme.reportsBlue else theme.reportsRed,
  text = amount.formattedString(includeSign = false),
)

@Composable
private fun CompactPercent(
  percent: Percent,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = ScaleToFitText(
  modifier = modifier.padding(16.dp),
  color = theme.reportsBlue,
  text = percent.toString(decimalPlaces = 2),
)

@Composable
private fun RegularPerMonth(
  data: SummaryData.AveragePerMonth,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier,
  horizontalAlignment = Alignment.Start,
) {
  ShowAs(data, onAction)

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(
      modifier = Modifier.width(IntrinsicSize.Max),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SumColumn(data)
        FilterDisplay(theme)
      }

      HorizontalDivider(
        thickness = 2.dp,
        color = theme.pageText,
      )

      Text(
        modifier = Modifier.fillMaxWidth(),
        text = Strings.reportsSummaryNumMonths,
        fontSize = 25.sp,
        textAlign = TextAlign.Center,
        color = theme.pageText,
      )
    }

    HorizontalSpacer(10.dp)
    Equals()
    HorizontalSpacer(10.dp)

    Column(
      modifier = Modifier.width(IntrinsicSize.Max),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = data.total.formattedString(),
        fontSize = 30.sp,
        textAlign = TextAlign.Center,
        color = theme.pageText,
      )

      HorizontalDivider(
        thickness = 2.dp,
        color = theme.pageText,
      )

      Text(
        modifier = Modifier.fillMaxWidth(),
        text = "%.2f".format(data.numMonths),
        fontSize = 30.sp,
        textAlign = TextAlign.Center,
        color = theme.pageText,
      )
    }

    HorizontalSpacer(10.dp)
    Equals()
    HorizontalSpacer(10.dp)

    Box(
      modifier = Modifier.width(width = 250.dp),
    ) {
      CompactAmount(data.average, theme = theme)
    }
  }
}

@Composable
private fun RegularPerTransaction(
  data: SummaryData.AveragePerTransaction,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier,
  horizontalAlignment = Alignment.Start,
) {
  ShowAs(data, onAction)

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(
      modifier = Modifier.width(IntrinsicSize.Max),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SumColumn(data)
        FilterDisplay(theme)
      }

      HorizontalDivider(
        thickness = 2.dp,
        color = theme.pageText,
      )

      Text(
        modifier = Modifier.fillMaxWidth(),
        text = Strings.reportsSummaryNumTransactions,
        fontSize = 25.sp,
        textAlign = TextAlign.Center,
        color = theme.pageText,
      )
    }

    HorizontalSpacer(10.dp)
    Equals()
    HorizontalSpacer(10.dp)

    Column(
      modifier = Modifier.width(IntrinsicSize.Max),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = data.total.formattedString(),
        fontSize = 30.sp,
        textAlign = TextAlign.Center,
        color = theme.pageText,
      )

      HorizontalDivider(
        thickness = 2.dp,
        color = theme.pageText,
      )

      Text(
        modifier = Modifier.fillMaxWidth(),
        text = data.numTransactions.toString(),
        fontSize = 30.sp,
        textAlign = TextAlign.Center,
        color = theme.pageText,
      )
    }

    HorizontalSpacer(10.dp)
    Equals()
    HorizontalSpacer(10.dp)

    Box(
      modifier = Modifier.width(width = 250.dp),
    ) {
      CompactAmount(data.average, theme = theme)
    }
  }
}

@Composable
private fun RegularPercent(
  data: SummaryData.Percentage,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier,
  horizontalAlignment = Alignment.Start,
) {
  ShowAs(data, onAction)
  DivisorCheckbox(data, onAction)

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(
      modifier = Modifier.width(IntrinsicSize.Max),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SumColumn(data)
        FilterDisplay(theme)
      }

      HorizontalDivider(
        thickness = 2.dp,
        color = theme.pageText,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SumColumn(data.divisor)
        FilterDisplay(theme)
      }
    }

    HorizontalSpacer(10.dp)
    Equals()
    HorizontalSpacer(10.dp)

    Column(
      modifier = Modifier.width(IntrinsicSize.Max),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = data.numerator.toString(),
        fontSize = 30.sp,
        textAlign = TextAlign.Center,
        color = theme.pageText,
      )

      HorizontalDivider(
        thickness = 2.dp,
        color = theme.pageText,
      )

      Text(
        modifier = Modifier.fillMaxWidth(),
        text = data.denominator.toString(),
        fontSize = 30.sp,
        textAlign = TextAlign.Center,
        color = theme.pageText,
      )
    }

    HorizontalSpacer(10.dp)
    Equals()
    HorizontalSpacer(10.dp)

    Box(
      modifier = Modifier.width(width = 250.dp),
    ) {
      CompactPercent(data.percent, theme = theme)
    }
  }
}

@Composable
private fun RegularSum(
  data: SummaryData.Sum,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier,
  horizontalAlignment = Alignment.Start,
) {
  ShowAs(data, onAction)

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    SumColumn(data)
    FilterDisplay(theme)
    Equals()

    Box(
      modifier = Modifier.width(width = 250.dp),
    ) {
      CompactAmount(data.value, theme = theme)
    }
  }
}

@Composable
private fun ShowAs(
  data: SummaryData,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Row(
  modifier = modifier.wrapContentSize(),
  verticalAlignment = Alignment.CenterVertically,
) {
  Text(text = Strings.reportsSummaryShowAs)

  HorizontalSpacer(5.dp)

  val chartTypeMap: ImmutableMap<String, SummaryChartType> = SummaryChartType
    .entries
    .associateBy { string(it) }
    .toImmutableMap()

  val currentType = when (data) {
    is SummaryData.AveragePerMonth -> SummaryChartType.AveragePerMonth
    is SummaryData.AveragePerTransaction -> SummaryChartType.AveragePerTransaction
    is SummaryData.Percentage -> SummaryChartType.Percentage
    is SummaryData.Sum -> SummaryChartType.Sum
  }

  ExposedDropDownMenu(
    modifier = Modifier.wrapContentSize(),
    value = string(currentType),
    options = chartTypeMap.keys.toImmutableList(),
    onValueChange = { string ->
      val type = chartTypeMap.getValue(string)
      onAction(Action.SetSummaryType(type))
    },
    theme = theme,
  )
}

@Composable
private fun SumColumn(
  range: DateRange,
) = Column(
  modifier = Modifier.wrapContentSize(),
  horizontalAlignment = Alignment.CenterHorizontally,
) {
  range.start?.let {
    Text(text = string(it))
  }

  Icon(
    modifier = Modifier.size(50.dp),
    imageVector = AktualIcons.Sum,
    contentDescription = null,
  )

  range.end?.let {
    Text(text = string(it))
  }
}

@Composable
private fun DivisorCheckbox(
  data: SummaryData.Percentage,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Checkbox(
      modifier = Modifier.minimumInteractiveComponentSize(),
      checked = data.divisor is PercentageDivisor.AllTime,
      onCheckedChange = { newValue -> onAction(Action.SetAllTimeDivisor(newValue)) },
    )

    HorizontalSpacer(4.dp)

    Text(
      modifier = Modifier.weight(1f),
      text = Strings.reportsSummaryAllTimeDivisor,
    )
  }
}

@Composable
private fun FilterDisplay(
  theme: Theme,
) = Row(
  modifier = Modifier.wrapContentSize(),
  verticalAlignment = Alignment.CenterVertically,
) {
  Icon(
    modifier = Modifier.size(50.dp),
    imageVector = AktualIcons.OpenBracket,
    contentDescription = null,
  )

  // TODO handle filtering here https://github.com/jonapoul/aktual/issues/361
  Text(
    text = Strings.reportsSummaryAll,
    fontSize = 25.sp,
    color = theme.pageTextPositive,
  )

  Icon(
    modifier = Modifier.size(50.dp),
    imageVector = AktualIcons.CloseBracket,
    contentDescription = null,
  )
}

@Composable
private fun Equals(modifier: Modifier = Modifier) = Icon(
  modifier = modifier.size(50.dp),
  imageVector = AktualIcons.Equals,
  contentDescription = null,
)

// E.g. "Jan 24"
@Composable
private fun string(date: LocalDate): String {
  val month = date.month.stringShort()
  val year = date.year.toString().substring(startIndex = 2)
  return "$month $year"
}

@Composable
private fun string(type: SummaryChartType): String = when (type) {
  SummaryChartType.Sum -> Strings.reportsSummarySum
  SummaryChartType.AveragePerMonth -> Strings.reportsSummaryPerMonth
  SummaryChartType.AveragePerTransaction -> Strings.reportsSummaryPerTransaction
  SummaryChartType.Percentage -> Strings.reportsSummaryPercentage
}

@Preview
@Composable
private fun PreviewSummaryChart(
  @PreviewParameter(SummaryChartProvider::class) params: ThemedParams<SummaryChartParams>,
) = PreviewWithColorScheme(schemeType = params.type, isPrivacyEnabled = params.data.private) {
  SummaryChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp)
      .let { m -> if (params.data.compact) m.height(300.dp) else m }
      .padding(5.dp),
    data = params.data.data,
    compact = params.data.compact,
    onAction = {},
  )
}

private data class SummaryChartParams(
  val data: SummaryData,
  val compact: Boolean,
  val private: Boolean = false,
)

private class SummaryChartProvider : ThemedParameterProvider<SummaryChartParams>(
  SummaryChartParams(SUM_DATA, compact = true),
  SummaryChartParams(SUM_DATA, compact = false),
  SummaryChartParams(SUM_DATA, compact = false, private = true),
  SummaryChartParams(PERCENT_DATA, compact = true),
  SummaryChartParams(PER_MONTH_DATA, compact = false),
  SummaryChartParams(PER_TRANSACTION_DATA, compact = false),
  SummaryChartParams(PER_TRANSACTION_DATA, compact = false, private = true),
  SummaryChartParams(PERCENT_DATA.copy(divisor = PercentageDivisor.AllTime), compact = false, private = true),
  SummaryChartParams(
    PERCENT_DATA.copy(divisor = PercentageDivisor.Specific(START_DATE, END_DATE)),
    compact = false,
    private = true,
  ),
)

private val SUM_DATA = SummaryData.Sum(
  title = "My Sum Summary",
  start = START_DATE,
  end = END_DATE,
  value = AMOUNT,
)

private val PER_MONTH_DATA = SummaryData.AveragePerMonth(
  title = "My Per-Month Summary",
  start = START_DATE,
  end = END_DATE,
  numMonths = 18.19f,
  total = Amount(6198.55),
  average = Amount(340.70),
)

internal val PER_TRANSACTION_DATA = SummaryData.AveragePerTransaction(
  title = "My Per-Transaction Summary",
  start = START_DATE,
  end = END_DATE,
  numTransactions = 1327,
  total = Amount(6198.55),
  average = Amount(340.70),
)

private val PERCENT_DATA = SummaryData.Percentage(
  title = "My Percent Summary",
  start = START_DATE,
  end = END_DATE,
  numerator = Amount(6198.55),
  denominator = Amount(4043.87),
  percent = 153.28.percent,
  divisor = PercentageDivisor.AllTime,
)
