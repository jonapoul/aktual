/**
 * Copyright 2025 Jon Poulton
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
package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.vm.SpendingComparison
import aktual.budget.reports.vm.SpendingData
import aktual.budget.reports.vm.SpendingDayNumber
import aktual.core.ui.AktualTypography
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import aktual.core.ui.WrapWidthTable
import aktual.core.ui.formattedString
import aktual.core.ui.isInPreview
import aktual.core.ui.stringShort
import aktual.l10n.Strings
import alakazam.kotlin.compose.HorizontalSpacer
import alakazam.kotlin.core.capitalized
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.multiplatform.common.Fill
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt

@Composable
internal fun SpendingChart(
  data: SpendingData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  includeHeader: Boolean = true,
) = Column(
  modifier = modifier,
) {
  if (includeHeader) {
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
  }

  Chart(
    modifier = if (compact) Modifier.fillMaxSize() else Modifier.weight(1f),
    data = data,
    compact = compact,
    theme = theme,
  )

  if (!compact) {
    Footer(
      title = Strings.reportsSpendingFooterTitle,
      text = Strings.reportsSpendingFooter,
    )
  }
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
    modifier = modifier.fillMaxHeight(),
    modelProducer = modelProducer,
    scrollState = rememberVicoScrollState(scrollEnabled = false),
    chart = rememberCartesianChart(
      rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(
          LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(Fill(theme.reportsGreen)),
            areaFill = LineCartesianLayer.AreaFill.single(
              fill = Fill(theme.reportsGreen.copy(alpha = 0.2f)),
            ),
          ),
        ),
      ),
      rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(
          LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(Fill(theme.reportsGray)),
            stroke = LineCartesianLayer.LineStroke.Dashed(thickness = 1.dp),
            areaFill = LineCartesianLayer.AreaFill.single(
              fill = Fill(theme.reportsGray.copy(alpha = 0.2f)),
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
      style = AktualTypography.bodyLarge,
    )
    Text(
      text = Strings.reportsSpendingDateRange(
        data.targetMonth.stringShort(),
        data.comparison.string(),
      ),
      color = theme.pageTextSubdued,
      style = AktualTypography.bodyMedium,
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

  val style = AktualTypography.bodySmall.copy(textAlign = TextAlign.End)
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
    style = AktualTypography.bodySmall,
  )
}

@Composable
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
