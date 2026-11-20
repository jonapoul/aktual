package aktual.core.ui

import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AktualProgressIndicator(
  modifier: Modifier = Modifier,
  size: Dp = ProgressIndicatorSize,
  color: Color = colors.pageText,
  strokeWidth: Dp = size / STROKE_RATIO,
) =
  CircularProgressIndicator(
    modifier = modifier.size(size),
    color = color,
    strokeWidth = strokeWidth,
  )

@Composable
fun AktualProgressIndicator(
  progress: () -> Float,
  modifier: Modifier = Modifier,
  size: Dp = ProgressIndicatorSize,
  color: Color = colors.pageText,
  strokeWidth: Dp = size / STROKE_RATIO,
) =
  CircularProgressIndicator(
    progress = progress,
    modifier = modifier.size(size),
    color = color,
    strokeWidth = strokeWidth,
  )

@Composable
fun LoadingScreen(modifier: Modifier = Modifier, color: Color = colors.pageText) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Center) {
    AktualProgressIndicator(color = color)
  }
}

private val ProgressIndicatorSize = 40.dp
private const val STROKE_RATIO = 10

@Preview
@Composable
private fun PreviewSizes(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column(horizontalAlignment = CenterHorizontally) {
      AktualProgressIndicator(size = 20.dp)
      AktualProgressIndicator()
      AktualProgressIndicator(size = 80.dp)
      AktualProgressIndicator(progress = { 0.6f })
    }
  }

@Preview
@Composable
private fun PreviewLoadingScreen(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { LoadingScreen() }
