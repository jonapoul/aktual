package aktual.budget.reports.ui.charts

import aktual.budget.reports.ui.Tags
import aktual.core.theme.DarkColors
import aktual.core.ui.PreviewWithColors
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.multiplatform.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberVicoScrollState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ChartZoomGestureTest {
  private val zoom = ChartZoomState()

  @Test
  fun `Long press then drag zooms`() = runComposeUiTest {
    // given
    setChart()

    // when long pressing then dragging across part of the chart
    onNodeWithTag(Tags.ZoomableChart).performTouchInput {
      down(centerLeft.copy(x = width * 0.3f))
      advanceEventTime(LONG_PRESS_MS)
      moveTo(center.copy(x = width * 0.6f))
      up()
    }

    // then it's zoomed to the snapped x values under the pointer
    assertThat(zoom.range).isEqualTo(3.0..6.0)
  }

  @Test
  fun `Dragging without a long press doesn't zoom`() = runComposeUiTest {
    setChart()

    onNodeWithTag(Tags.ZoomableChart).performTouchInput {
      down(centerLeft.copy(x = width * 0.3f))
      moveTo(center.copy(x = width * 0.6f))
      up()
    }

    assertThat(zoom.range).isNull()
  }

  @Test
  fun `Long press without dragging doesn't zoom`() = runComposeUiTest {
    setChart()

    onNodeWithTag(Tags.ZoomableChart).performTouchInput {
      down(center)
      advanceEventTime(LONG_PRESS_MS)
      up()
    }

    assertThat(zoom.range).isNull()
  }

  private fun ComposeUiTest.setChart() = setContent {
    PreviewWithColors(DarkColors) {
      ZoomableChart(state = zoom, enabled = true, modifier = Modifier.size(400.dp)) { chartModifier
        ->
        CartesianChartHost(
          modifier = chartModifier,
          model = MODEL,
          scrollState = rememberVicoScrollState(scrollEnabled = false),
          chart =
            rememberCartesianChart(
              rememberLineCartesianLayer(),
              decorations = listOf(rememberChartZoomDecoration(zoom)),
            ),
        )
      }
    }
  }

  private companion object {
    const val LONG_PRESS_MS = 1_000L

    // x values 0 to 10, evenly spread across the chart's width
    val MODEL = CartesianChartModel(LineCartesianLayerModel.build { series((0..10).toList()) })
  }
}
