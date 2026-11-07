package aktual.core.ui

import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified

/**
 * A bar vertically centred in the height of one line of [style], so a shimmer placeholder takes the
 * same height as the text it stands in for. Give it a width with [modifier].
 */
@Composable
fun ShimmerLine(
  style: TextStyle,
  modifier: Modifier = Modifier,
  barHeight: Dp = style.barHeight(),
) {
  Box(modifier = modifier, contentAlignment = CenterStart) {
    Text(text = "", style = style, maxLines = 1)
    ShimmerBlock(Modifier.fillMaxWidth().height(barHeight))
  }
}

/** A solid shimmer placeholder for non-text content like icons and charts */
@Composable
fun ShimmerBlock(modifier: Modifier = Modifier, shape: Shape = CardShape) {
  Box(modifier = modifier.background(colors.pageText, shape))
}

/** A shimmer placeholder the same height as a [Chip] of [size] showing one line of [textStyle] */
@Composable
fun ShimmerChip(textStyle: TextStyle, modifier: Modifier = Modifier, size: ChipSize = Regular) {
  Box(
    modifier = modifier.background(colors.pageText, size.shape).padding(size.contentPadding),
  ) {
    Text(text = "", style = textStyle, maxLines = 1)
  }
}

@Composable
@ReadOnlyComposable
private fun TextStyle.barHeight(): Dp =
  if (fontSize.isSpecified) with(LocalDensity.current) { fontSize.toDp() } else 16.dp
