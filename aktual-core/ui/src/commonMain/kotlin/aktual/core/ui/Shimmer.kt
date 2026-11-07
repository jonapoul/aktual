package aktual.core.ui

import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle

/**
 * A bar vertically centred in the height of one line of [style], so a shimmer placeholder takes the
 * same height as the text it stands in for. Give it a width with [modifier].
 */
@Composable
fun ShimmerLine(style: TextStyle, modifier: Modifier = Modifier) {
  Box(modifier = modifier) {
    Text(text = "", style = style, maxLines = 1)
    Box(modifier = Modifier.matchParentSize(), contentAlignment = CenterStart) {
      ShimmerBlock(Modifier.fillMaxWidth().fillMaxHeight(BAR_FRACTION))
    }
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

// Roughly the font size as a share of its line height
private const val BAR_FRACTION = 0.7f
