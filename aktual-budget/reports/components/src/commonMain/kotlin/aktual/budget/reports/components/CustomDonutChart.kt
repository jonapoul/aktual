package aktual.budget.reports.components

import aktual.budget.reports.vm.CustomMetric
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.isInPreview
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.MeasuringContext
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.PieSize
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.PieValueFormatter
import com.patrykandpatrick.vico.compose.pie.data.pieModel
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.runBlocking

// packages/desktop-client/src/components/reports/graphs/DonutGraph.tsx: one slice per group, or per
// interval when grouped by interval. Slices come from donutSlices(), so none are negative
@Composable
internal fun CustomDonutChart(
  slices: ImmutableList<CustomBar>,
  metric: CustomMetric,
  compact: Boolean,
  modifier: Modifier = Modifier,
  onDrilldown: OnDrilldown? = null,
) {
  val modelProducer = remember { PieChartModelProducer() }
  if (isInPreview()) {
    runBlocking { modelProducer.populateSlices(slices, metric) }
  } else {
    LaunchedEffect(slices, metric) { modelProducer.populateSlices(slices, metric) }
  }

  val labelColor = colors.reportsInnerLabel
  val label =
    if (compact) {
      null
    } else {
      PieChart.SliceLabel.Inside(
        rememberTextComponent(
          style = TextStyle(color = labelColor, fontSize = textSizes.small),
        ),
      )
    }
  val sliceProvider =
    remember(slices, metric, label) {
      val total = slices.sumOf { it.amount.plotted(metric).toDouble() }
      PieChart.SliceProvider.series(
        slices.map { slice ->
          val share = slice.amount.plotted(metric).toDouble() / total
          PieChart.Slice(
            fill = Fill(slice.color),
            label = label?.takeIf { share >= LABEL_THRESHOLD },
          )
        },
      )
    }

  val values = remember(slices, metric) { slices.map { it.amount.plotted(metric).toDouble() } }
  val tapModifier =
    if (onDrilldown == null) {
      Modifier
    } else {
      Modifier.pointerInput(slices, values, onDrilldown) {
        detectTapGestures { tap ->
          val center = Offset(size.width / 2f, size.height / 2f)
          val radius = minOf(size.width, size.height) / 2f
          if ((tap - center).getDistance() <= radius) {
            sliceAt(tap, center, values)?.let { onDrilldown(slices[it].drilldown) }
          }
        }
      }
    }

  PieChartHost(
    modifier = modifier.then(tapModifier),
    modelProducer = modelProducer,
    animationSpec = chartAnimationSpec(compact),
    chart =
      rememberPieChart(
        sliceProvider = sliceProvider,
        innerSize = DonutHole,
        valueFormatter = PercentFormatter,
      ),
  )
}

// DonutGraph.tsx can't draw negative slices, so leave out anything not above zero once debts are
// flipped positive
internal fun List<CustomBar>.donutSlices(metric: CustomMetric): ImmutableList<CustomBar> {
  val slices = filter { it.amount.plotted(metric) > Zero }
  return slices.toImmutableList()
}

private suspend fun PieChartModelProducer.populateSlices(
  slices: List<CustomBar>,
  metric: CustomMetric,
) = runTransaction {
  if (slices.isNotEmpty()) {
    pieModel { series(slices.map { it.amount.plotted(metric).toDouble() }) }
  }
}

// Upstream's inner radius is about half the outer one
private val DonutHole =
  object : PieSize.Inner {
    override fun getRadius(
      context: MeasuringContext,
      availableWidth: Float,
      availableHeight: Float,
    ): Float = minOf(availableWidth, availableHeight) / 2 * HOLE_FRACTION
  }

private val PercentFormatter = PieValueFormatter { context, value, _ ->
  "${(value / context.model.sum * 100).roundToInt()}%"
}

private const val HOLE_FRACTION = 0.5f

// Upstream skips labels on slices under 5% of the donut
private const val LABEL_THRESHOLD = 0.05
