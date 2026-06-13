package aktual.budget.tags.ui

import androidx.compose.ui.graphics.Color

// pick black or white text for legibility on [this], using the brightness formula from
// https://www.w3.org/TR/AERT/#color-contrast — adapted from upstream's getTagCSSColors in
// packages/desktop-client/src/hooks/useTagCSS.ts
@Suppress("MagicNumber")
internal fun Color.contrastingTextColor(): Color {
  val brightness = (red * 299 + green * 587 + blue * 114) * 255 / 1000
  return if (brightness >= 125) Color.Black else Color.White
}
