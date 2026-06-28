package aktual.budget.tags.vm

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

// Single source of truth for hex<->Color in the tags feature: the loader, the editor's save path
// and the colour-picker field all go through these. Upstream's ColorPicker emits "#RRGGBB"
// (uppercase, no alpha), so that's the canonical on-disk form.

private const val RGB_SHORTHAND_LENGTH = 3
private const val RGB_HEX_LENGTH = 6
private const val ARGB_HEX_LENGTH = 8

// Parses a CSS hex colour into an opaque Color, or null if it isn't valid hex. Accepts "#rgb"
// shorthand, "#rrggbb" and "#aarrggbb", with or without the leading '#'. Alpha is normalised away
// (storage is "#rrggbb"), so the swatch the user sees always matches what gets stored
@Suppress("MagicNumber")
fun String.toColorOrNull(): Color? {
  val hex = trim().removePrefix("#")
  val rrggbb =
    when (hex.length) {
      RGB_SHORTHAND_LENGTH -> hex.map { "$it$it" }.joinToString(separator = "")
      RGB_HEX_LENGTH -> hex
      ARGB_HEX_LENGTH -> hex.substring(2)
      else -> return null
    }
  val rgb = rrggbb.toLongOrNull(radix = 16) ?: return null
  return Color(rgb or 0xFF000000L)
}

// Inverse of [toColorOrNull] — formats as "#RRGGBB" (uppercase, opaque) for storage and display
@Suppress("MagicNumber")
fun Color.toHex(): String {
  fun Float.hex2() =
    (this * 255f).roundToInt().coerceIn(0, 255).toString(radix = 16).padStart(2, '0')
  return "#${red.hex2()}${green.hex2()}${blue.hex2()}".uppercase()
}
