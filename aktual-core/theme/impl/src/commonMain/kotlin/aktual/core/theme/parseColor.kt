package aktual.core.theme

import androidx.compose.ui.graphics.Color

class ColorParseException(message: String) : Exception(message)

private val HexRegex = "#[0-9A-Fa-f]{3,8}".toRegex()
private val RgbRegex = """rgb\(\s*(\d+),\s*(\d+),\s*(\d+)\s*\)""".toRegex()
private val RgbaRegex = """rgba\(\s*(\d+),\s*(\d+),\s*(\d+),\s*([\d.]+)\s*\)""".toRegex()
private val HslRegex = """hsl\(\s*([\d.]+),\s*([\d.]+)%,\s*([\d.]+)%\s*\)""".toRegex()
private val HslaRegex = """hsla\(\s*([\d.]+),\s*([\d.]+)%,\s*([\d.]+)%,\s*([\d.]+)\s*\)""".toRegex()

internal fun String.parseColor(): Color =
  when {
    this == "transparent" -> Color.Transparent
    matches(HexRegex) -> parseHex()
    matches(RgbRegex) -> parseRgb()
    matches(RgbaRegex) -> parseRgba()
    matches(HslRegex) -> parseHsl()
    matches(HslaRegex) -> parseHsla()
    else -> throw ColorParseException("Not sure how to parse '$this' as Color")
  }

private fun String.parseRgb(): Color {
  val (_, r, g, b) = requireNotNull(RgbRegex.find(this)).groupValues
  return Color(red = r.toInt() / 255f, green = g.toInt() / 255f, blue = b.toInt() / 255f)
}

private fun String.parseRgba(): Color {
  val (_, r, g, b, a) = requireNotNull(RgbaRegex.find(this)).groupValues
  return Color(
    red = r.toInt() / 255f,
    green = g.toInt() / 255f,
    blue = b.toInt() / 255f,
    alpha = a.toFloat(),
  )
}

private fun String.parseHsl(): Color {
  val (_, h, s, l) = requireNotNull(HslRegex.find(this)).groupValues
  return hslToColor(h.toFloat(), s.toFloat() / 100f, l.toFloat() / 100f, alpha = 1f)
}

private fun String.parseHsla(): Color {
  val (_, h, s, l, a) = requireNotNull(HslaRegex.find(this)).groupValues
  return hslToColor(h.toFloat(), s.toFloat() / 100f, l.toFloat() / 100f, a.toFloat())
}

// https://www.w3.org/TR/css-color-3/#hsl-color
private fun hslToColor(h: Float, s: Float, l: Float, alpha: Float): Color {
  val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
  val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
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

private fun String.parseHex(): Color {
  val hex = removePrefix("#")
  val argb =
    when (hex.length) {
      3 -> "FF" + hex.map { "$it$it" }.joinToString("")
      6 -> "FF$hex"
      8 -> hex.takeLast(2) + hex.take(6) // CSS: RRGGBBAA → AARRGGBB
      else -> throw ColorParseException("Invalid hex color: $this")
    }
  return Color(argb.toLong(16))
}
