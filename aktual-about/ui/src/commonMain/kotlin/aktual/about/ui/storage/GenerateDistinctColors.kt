package aktual.about.ui.storage

import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.PreviewWithThemedParams
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun rememberDistinctColors(
  count: Int,
  theme: Theme = LocalTheme.current,
  saturation: Float = 0.9f,
  lightness: Float = 0.35f,
): ImmutableList<Color> =
  remember(count, theme, saturation, lightness) {
    generateDistinctColors(theme, count, saturation, lightness).toImmutableList()
  }

/**
 * Generates [count] visually distinct colors evenly spaced around the HSL hue wheel. Returns ARGB
 * longs with full opacity.
 */
internal fun generateDistinctColors(
  theme: Theme,
  count: Int,
  saturation: Float = 0.9f,
  lightness: Float = 0.35f,
): List<Color> {
  require(count > 0) { "Count must be positive" }
  val lightness = if (theme.isLight) lightness else 1f - lightness
  return List(count) { index ->
    val hue = (index * 360f / count) % 360f
    Color(hslToArgb(hue, saturation, lightness))
  }
}

private fun hslToArgb(h: Float, s: Float, l: Float): Long {
  val c = (1 - abs(2 * l - 1)) * s
  val x = c * (1 - abs((h / 60f) % 2 - 1))
  val m = l - c / 2

  val (r1, g1, b1) =
    when {
      h < 60f -> Triple(c, x, 0f)
      h < 120f -> Triple(x, c, 0f)
      h < 180f -> Triple(0f, c, x)
      h < 240f -> Triple(0f, x, c)
      h < 300f -> Triple(x, 0f, c)
      else -> Triple(c, 0f, x)
    }

  val r = ((r1 + m) * 255).roundToInt().coerceIn(0, 255)
  val g = ((g1 + m) * 255).roundToInt().coerceIn(0, 255)
  val b = ((b1 + m) * 255).roundToInt().coerceIn(0, 255)

  // ARGB: 0xFFRRGGBB
  return (0xFF shl 24 or (r shl 16) or (g shl 8) or b).toLong()
}

@Preview
@Composable
private fun PreviewDistinctColors(
  @PreviewParameter(CountParameters::class) params: ThemedParams<Int>
) =
  PreviewWithThemedParams(params) {
    val colors = rememberDistinctColors(count = this, theme = params.theme)

    Column {
      colors.fastForEach { color ->
        Box(
          modifier = Modifier.height(20.dp).width(40.dp).background(color),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "#%06X".format(color.toArgb() and 0xFFFFFF),
            color = params.theme.formInputTextSelected,
            fontSize = 8.sp,
            textAlign = TextAlign.Center,
          )
        }
      }
    }
  }

private class CountParameters : ThemedParameterProvider<Int>(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
