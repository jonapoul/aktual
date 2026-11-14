@file:Suppress("MagicNumber", "MatchingDeclarationName")

package aktual.core.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

class ColorParseException(message: String) : Exception(message)

private val HexRegex = "#[0-9A-Fa-f]{3,8}".toRegex()
private val RgbRegex =
  """rgba?\(\s*(\d+%?)\s*,\s*(\d+%?)\s*,\s*(\d+%?)\s*(?:,\s*([\d.]+)\s*)?\)"""
    .toRegex(RegexOption.IGNORE_CASE)
private val HslRegex =
  """hsla?\(\s*([\d.]+)\s*,\s*([\d.]+)%\s*,\s*([\d.]+)%\s*(?:,\s*([\d.]+)\s*)?\)"""
    .toRegex(RegexOption.IGNORE_CASE)

fun String.parseColor(): Color =
  parseColorOrNull() ?: throw ColorParseException("Not sure how to parse '$this' as Color")

fun String.parseColorOrNull(): Color? =
  when {
    equals("transparent", ignoreCase = true) -> Transparent
    matches(HexRegex) -> parseHex()
    matches(RgbRegex) -> parseRgb()
    matches(HslRegex) -> parseHsl()
    else -> null
  }

private fun String.parseRgb(): Color {
  val (_, r, g, b, a) = requireNotNull(RgbRegex.find(this)).groupValues
  return Color(
    red = r.toChannel(),
    green = g.toChannel(),
    blue = b.toChannel(),
    alpha = a.toAlpha(),
  )
}

private fun String.toChannel(): Float =
  if (endsWith("%")) removeSuffix("%").toFloat() / 100f else toInt() / 255f

private fun String.toAlpha(): Float = if (isEmpty()) 1f else toFloat()

private fun String.parseHsl(): Color {
  val (_, h, s, l, a) = requireNotNull(HslRegex.find(this)).groupValues
  return hslToColor(h.toFloat(), s.toFloat() / 100f, l.toFloat() / 100f, a.toAlpha())
}

// https://www.w3.org/TR/css-color-3/#hsl-color
private fun hslToColor(h: Float, s: Float, l: Float, alpha: Float): Color {
  val c = (1f - abs(2f * l - 1f)) * s
  val x = c * (1f - abs(h / 60f % 2f - 1f))
  val m = l - c / 2f
  val (r, g, b) =
    when {
      h < 60f -> Triple(c, x, 0f)
      h < 120f -> Triple(x, c, 0f)
      h < 180f -> Triple(0f, c, x)
      h < 240f -> Triple(0f, x, c)
      h < 300f -> Triple(x, 0f, c)
      else -> Triple(c, 0f, x)
    }
  return Color(red = r + m, green = g + m, blue = b + m, alpha = alpha)
}

private fun String.parseHex(): Color? {
  val hex = removePrefix("#")
  val argb =
    when (hex.length) {
      3 -> "FF" + hex.map { "$it$it" }.joinToString("")
      6 -> "FF$hex"
      8 -> hex.takeLast(2) + hex.take(6) // CSS: RRGGBBAA → AARRGGBB
      else -> return null
    }
  return Color(argb.toLong(16))
}
