package actual.budget.reports.ui.charts

import actual.budget.model.Amount
import actual.budget.reports.ui.charts.PreviewShared.WIDTH
import actual.budget.reports.vm.SpendingComparison
import actual.budget.reports.vm.SpendingData
import actual.budget.reports.vm.SpendingDayNumber
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import actual.core.ui.WrapWidthTable
import actual.core.ui.formattedString
import actual.core.ui.isInPreview
import actual.core.ui.stringShort
import actual.l10n.Strings
import alakazam.android.ui.compose.HorizontalSpacer
import alakazam.kotlin.core.capitalized
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.dashed
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer.AreaFill
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer.LineFill
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer.LineProvider
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer.LineStroke
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt

@Composable
internal fun SpendingChart(
  data: SpendingData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier,
) {
  if (compact) {
    CompactHeader(
      modifier = Modifier.padding(start = HEADER_PADDING, end = HEADER_PADDING, top = HEADER_PADDING),
      data = data,
      theme = theme,
    )
  } else {
    RegularLegend(
      data = data,
      theme = theme,
    )
  }

  Chart(
    data = data,
    compact = compact,
    theme = theme,
  )
}

@Composable
private fun Chart(
  data: SpendingData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val modelProducer = remember { CartesianChartModelProducer() }

  if (isInPreview()) {
    runBlocking { modelProducer.populate(data) }
  } else {
    LaunchedEffect(data) { modelProducer.populate(data) }
  }

  val label = axisLabelComponent(compact)
  val tick = axisTickComponent(compact)
  val guideline = axisGuidelineComponent(compact)
  val line = axisLineComponent(compact)

  CartesianChartHost(
    modifier = modifier,
    modelProducer = modelProducer,
    scrollState = rememberVicoScrollState(scrollEnabled = false),
    chart = rememberCartesianChart(
      rememberLineCartesianLayer(
        lineProvider = LineProvider.series(
          LineCartesianLayer.rememberLine(
            fill = LineFill.single(fill(theme.reportsGreen)),
            areaFill = AreaFill.single(
              fill = fill(theme.reportsGreen.copy(alpha = 0.2f)),
            ),
          ),
        ),
      ),
      rememberLineCartesianLayer(
        lineProvider = LineProvider.series(
          LineCartesianLayer.rememberLine(
            fill = LineFill.single(fill(theme.reportsGray)),
            stroke = LineStroke.dashed(thickness = 1.dp),
            areaFill = AreaFill.single(
              fill = fill(theme.reportsGray.copy(alpha = 0.2f)),
            ),
          ),
        ),
      ),
      startAxis = VerticalAxis.rememberStart(
        line = line,
        guideline = guideline,
        label = label,
        tick = tick,
        valueFormatter = amountYAxisFormatter(),
        itemPlacer = remember { VerticalAxis.ItemPlacer.step() },
      ),
      bottomAxis = HorizontalAxis.rememberBottom(
        line = line,
        guideline = guideline,
        label = label,
        tick = tick,
        valueFormatter = xAxisFormatter(),
        itemPlacer = remember { HorizontalAxis.ItemPlacer.segmented() },
      ),
      marker = if (compact) null else rememberMarker(),
    ),
  )
}

@Composable
private fun CompactHeader(
  data: SpendingData,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Row(
  modifier = modifier,
  verticalAlignment = Alignment.CenterVertically,
) {
  Column(
    modifier = Modifier.weight(1f),
    horizontalAlignment = Alignment.Start,
  ) {
    Text(
      text = data.title,
      color = theme.pageText,
      style = MaterialTheme.typography.bodyLarge,
    )
    Text(
      text = Strings.reportsSpendingDateRange(
        data.targetMonth.stringShort(),
        data.comparison.string(),
      ),
      color = theme.pageTextSubdued,
      style = MaterialTheme.typography.bodyMedium,
    )
  }

  Text(
    text = data.difference.formattedString(includeSign = true),
    fontWeight = FontWeight.Medium,
    color = if (data.difference.isPositive()) theme.errorText else theme.noticeTextLight,
  )
}

@Composable
private fun RegularLegend(
  data: SpendingData,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Row(
  modifier = modifier,
  verticalAlignment = Alignment.CenterVertically,
) {
  Column {
    LegendItem(
      text = data.targetMonth.stringShort(),
      color = theme.reportsGreen,
    )
    LegendItem(
      text = data.comparison.string().capitalized(),
      color = theme.reportsGray,
    )
  }

  HorizontalSpacer(weight = 1f)

  val style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.End)
  val mtdSpending = calculateMtdSpending(data)
  val padding = PaddingValues(horizontal = 4.dp)

  WrapWidthTable(
    modifier = Modifier.heightIn(max = 200.dp),
    ellipsize = false,
    data = persistentListOf(
      persistentListOf(
        Strings.reportsSpendingMtd(data.targetMonth.stringShort()),
        mtdSpending.target.formattedString(),
      ),
      persistentListOf(
        Strings.reportsSpendingMtd(data.comparison.string()),
        mtdSpending.comparison.formattedString(),
      ),
    ),
    textStyles = persistentListOf(
      style,
      style.copy(fontWeight = FontWeight.W600),
    ),
    paddings = persistentListOf(
      padding,
      padding,
    ),
  )
}

@Immutable
private data class MtdSpending(val target: Amount, val comparison: Amount)

@Stable
private fun calculateMtdSpending(data: SpendingData): MtdSpending {
  val lastDay = data.days.last { it.target != null }
  return MtdSpending(requireNotNull(lastDay.target), lastDay.comparison)
}

@Composable
private fun LegendItem(
  text: String,
  color: Color,
  modifier: Modifier = Modifier,
) = Row(
  modifier = modifier,
  verticalAlignment = Alignment.CenterVertically,
) {
  Box(
    modifier = Modifier
      .size(12.dp)
      .background(color, CircleShape),
  )

  HorizontalSpacer(4.dp)

  Text(
    text = text,
    style = MaterialTheme.typography.bodySmall,
  )
}

@Composable
@ReadOnlyComposable
private fun SpendingComparison.string() = when (this) {
  SpendingComparison.Average -> Strings.reportsSpendingAverage
  SpendingComparison.Budgeted -> Strings.reportsSpendingBudgeted
  is SpendingComparison.SingleMonth -> value.stringShort()
}

private const val END_DAY = 28
private val HEADER_PADDING = 8.dp

private suspend fun CartesianChartModelProducer.populate(data: SpendingData) = with(data) {
  val xValues = days.map { day ->
    when (val number = day.number) {
      is SpendingDayNumber.Specific -> number.number
      SpendingDayNumber.End -> END_DAY
    }
  }

  val targetAmounts = days.mapNotNull { it.target?.toDouble() }
  val comparisonAmounts = days.map { it.comparison.toDouble() }

  runTransaction {
    lineSeries {
      series(x = xValues.take(targetAmounts.size), y = targetAmounts)
    }

    lineSeries {
      series(x = xValues.take(comparisonAmounts.size), y = comparisonAmounts)
    }
  }
}

@Composable
private fun xAxisFormatter() = remember {
  CartesianValueFormatter { _, value, _ ->
    val int = value.roundToInt()
    if (int == END_DAY) "$END_DAY+" else "%02d".format(int)
  }
}

@Composable
private fun PreviewChart(
  data: SpendingData,
  compact: Boolean,
  isPrivacyEnabled: Boolean = false,
) = PreviewColumn(isPrivacyEnabled = isPrivacyEnabled) {
  SpendingChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp)
      .padding(5.dp),
    data = data,
    compact = compact,
  )
}

@Preview(widthDp = WIDTH)
@Composable
private fun MixedRegular() = PreviewChart(PreviewSpending.JUL_2025, compact = false)

@Preview(widthDp = WIDTH)
@Composable
private fun MixedCompact() = PreviewChart(PreviewSpending.JUL_2025, compact = true)
