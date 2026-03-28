package aktual.about.ui.storage

import aktual.core.model.Percent
import aktual.core.theme.Theme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemeParameters
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun PieChart(
  slices: ImmutableList<PieSlice>,
  modifier: Modifier = Modifier,
  strokeWidth: Dp = Dp.Unspecified, // use Unspecified for 18% of chart size, 0.dp for filled pie
  fontSize: TextUnit = 12.sp,
) {
  require(slices.isNotEmpty()) { "slices must not be empty" }

  val total =
    remember(slices) {
      slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(minimumValue = 0.0001f)
    }

  val textMeasurer = rememberTextMeasurer()

  Canvas(modifier = modifier) {
    drawPie(
      slices = slices,
      total = total,
      strokeWidthPx =
        if (strokeWidth == Dp.Unspecified) {
          min(size.width, size.height) * 0.18f
        } else {
          strokeWidth.toPx()
        },
      fontSize = fontSize,
      textMeasurer = textMeasurer,
    )
  }
}

@Immutable internal data class PieSlice(val value: Float, val color: Color)

@Suppress("MagicNumber")
private fun DrawScope.drawPie(
  slices: ImmutableList<PieSlice>,
  total: Float,
  strokeWidthPx: Float,
  fontSize: TextUnit,
  textMeasurer: TextMeasurer,
) {
  val radius = min(size.width, size.height) / 2f
  val inset = if (strokeWidthPx > 0f) strokeWidthPx / 2f else 0f
  val rect =
    Rect(left = inset, top = inset, right = size.width - inset, bottom = size.height - inset)
  var startAngle = -90f

  slices.fastForEach { slice ->
    val percent = Percent(numerator = slice.value, denominator = total)
    val sweep = 360f * slice.value / total
    val midAngle = startAngle + sweep / 2f
    val rad = Math.toRadians(midAngle.toDouble())

    if (strokeWidthPx > 0f) {
      drawArc(
        color = slice.color,
        startAngle = startAngle,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = rect.topLeft,
        size = rect.size,
        style = Stroke(width = strokeWidthPx),
      )
    } else {
      drawArc(
        color = slice.color,
        startAngle = startAngle,
        sweepAngle = sweep,
        useCenter = true,
        topLeft = rect.topLeft,
        size = rect.size,
      )
    }

    // Label position
    val labelRadius =
      if (strokeWidthPx > 0f) {
        radius - strokeWidthPx + strokeWidthPx / 2f
      } else {
        radius * 0.6f
      }

    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val lx = centerX + cos(rad).toFloat() * labelRadius
    val ly = centerY + sin(rad).toFloat() * labelRadius

    val layout =
      textMeasurer.measure(
        text = percent.toString(decimalPlaces = 0),
        style = TextStyle(color = Color.Black, fontSize = fontSize, fontWeight = FontWeight.Normal),
      )

    drawText(
      textLayoutResult = layout,
      topLeft = Offset(x = lx - layout.size.width / 2f, y = ly - layout.size.height / 2f),
    )

    startAngle += sweep
  }
}

@Preview
@Composable
private fun PreviewPieWithHole(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithColorScheme(theme) {
    val colors = rememberDistinctColors(count = 5)
    PieChart(
      modifier = Modifier.size(240.dp),
      strokeWidth = Dp.Unspecified,
      slices =
        colors
          .mapIndexed { index, color -> PieSlice((index + 1).toFloat(), color) }
          .toImmutableList(),
    )
  }

@Preview
@Composable
private fun PreviewPieFull(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithColorScheme(theme) {
    val colors = rememberDistinctColors(count = 10)
    PieChart(
      modifier = Modifier.size(240.dp),
      slices =
        colors
          .mapIndexed { index, color -> PieSlice((index + 1).toFloat(), color) }
          .toImmutableList(),
    )
  }
