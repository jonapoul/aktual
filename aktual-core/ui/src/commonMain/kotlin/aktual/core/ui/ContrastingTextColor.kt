package aktual.core.ui

import aktual.core.ui.AktualTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

// pick black or white text for legibility on [this] drawn over the page background
@Composable
@ReadOnlyComposable
fun Color.contrastingTextColor(): Color = contrastingTextColor(background = colors.backgroundPage)

// pick black or white text for legibility on [this] drawn over [background], using the brightness
// formula from https://www.w3.org/TR/AERT/#color-contrast - adapted from upstream's getTagCSSColors
// in packages/desktop-client/src/hooks/useTagCSS.ts
@Stable
@Suppress("MagicNumber")
fun Color.contrastingTextColor(background: Color): Color {
  val color = compositeOver(background)
  val brightness = (color.red * 299 + color.green * 587 + color.blue * 114) * 255 / 1000
  return if (brightness >= 125) Black else White
}
