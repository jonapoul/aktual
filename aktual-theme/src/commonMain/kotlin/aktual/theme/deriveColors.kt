package aktual.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

// Ported from upstream's themes/source/derive.ts, with culori's colour maths, so custom themes get
// the same accent and status colours upstream works out for them

internal data class Surfaces(val surface: Color, val page: Color, val mobilePage: Color)

internal data class AccentSet(
  val accent: Color,
  val accentContrast: Color,
  val accentHover: Color,
  val accentText: Color,
  val accentBorder: Color,
  val accentSubtle: Color,
  val accentSubtleText: Color,
)

internal data class StatusSet(
  val fill: Color,
  val fillSubtle: Color,
  val text: Color,
  val textOnSubtle: Color,
  val textStrong: Color,
)

internal const val TEXT_CONTRAST = 4.5
internal const val STRONG_TEXT_CONTRAST = 6.0
internal const val ENHANCED_TEXT_CONTRAST = 7.0
private const val SHAPE_CONTRAST = 3.0

private const val HOVER_STEP = 0.06
private const val LIGHTNESS_STEPS = 200
private const val SUBTLE_WEIGHT_ON_LIGHT = 0.12
private const val SUBTLE_WEIGHT_ON_DARK = 0.28
private const val FILL_LIGHTNESS_ON_LIGHT = 0.94
private const val FILL_LIGHTNESS_ON_DARK = 0.46
private const val FILL_SUBTLE_LIGHTNESS_ON_LIGHT = 0.97
private const val FILL_SUBTLE_LIGHTNESS_ON_DARK = 0.37

// culori bisects chroma down to a 2^13th of oklch's [0, 0.4] range
private const val CHROMA_RESOLUTION = 0.4 / 8192

internal fun deriveAccent(accent: Color, surfaces: Surfaces): AccentSet {
  val fill = accent.opaque()
  val contrast = pickContrast(fill)
  val surface = surfaces.surface.opaque()
  val weight = if (surface.toOklch().l > 0.5) SUBTLE_WEIGHT_ON_LIGHT else SUBTLE_WEIGHT_ON_DARK
  val subtle = mix(surface, fill, weight)
  val text =
    nearestWithContrast(
      fill,
      listOf(surfaces.surface, surfaces.page, surfaces.mobilePage, subtle),
      TEXT_CONTRAST,
    )
  return AccentSet(
    accent = fill,
    accentContrast = contrast,
    accentHover = hoverFill(fill, contrast),
    accentText = text,
    accentBorder =
      nearestWithContrast(fill, listOf(surfaces.surface, surfaces.page), SHAPE_CONTRAST),
    accentSubtle = subtle,
    accentSubtleText = text,
  )
}

internal fun deriveStatus(status: Color, surfaces: Surfaces): StatusSet {
  val input = status.toOklch()
  val onLight = surfaces.surface.toOklch().l > 0.5
  val fill = input.atLightness(if (onLight) FILL_LIGHTNESS_ON_LIGHT else FILL_LIGHTNESS_ON_DARK)
  val fillSubtle =
    input.atLightness(
      if (onLight) FILL_SUBTLE_LIGHTNESS_ON_LIGHT else FILL_SUBTLE_LIGHTNESS_ON_DARK,
    )
  val backgrounds = listOf(surfaces.surface, surfaces.page, surfaces.mobilePage)
  val (text, textOnSubtle, textStrong) =
    nearestWithContrasts(
      status,
      listOf(
        ContrastGoal(backgrounds + fill, TEXT_CONTRAST),
        ContrastGoal(backgrounds + fillSubtle, TEXT_CONTRAST),
        ContrastGoal(backgrounds + fill, STRONG_TEXT_CONTRAST),
      ),
    )
  return StatusSet(fill, fillSubtle, text, textOnSubtle, textStrong)
}

/** The colour nearest [color] in lightness that has [target] contrast against all [backgrounds]. */
internal fun nearestWithContrast(color: Color, backgrounds: List<Color>, target: Double): Color =
  nearestWithContrasts(color, listOf(ContrastGoal(backgrounds, target))).single()

/** White, or black when white doesn't reach text contrast and black does better. */
internal fun pickContrast(fill: Color): Color {
  val onWhite = contrast(Color.White, fill)
  if (onWhite >= TEXT_CONTRAST) return Color.White
  return if (onWhite >= contrast(Color.Black, fill)) Color.White else Color.Black
}

private data class ContrastGoal(val backgrounds: List<Color>, val target: Double)

private class Search(val target: Double, val luminances: List<Double>, start: Color) {
  var found: Color? = null
  var strongest: Color = start
  var strongestContrast = 0.0
}

// Steps through lightness from nearest to furthest, stopping once every goal is met. A goal that's
// never met takes the colour that came closest
private fun nearestWithContrasts(color: Color, goals: List<ContrastGoal>): List<Color> {
  val base = color.toOklch()
  val start = color.opaque()
  val searches = goals.map { goal ->
    Search(goal.target, goal.backgrounds.map { it.luminance() }, start)
  }

  fun measure(step: Color): Int {
    val own = step.luminance()
    var open = 0
    for (search in searches) {
      if (search.found != null) continue
      val lowest = search.luminances.minOf { (max(own, it) + 0.05) / (min(own, it) + 0.05) }
      if (lowest >= search.target) {
        search.found = step
      } else {
        open += 1
        if (lowest > search.strongestContrast) {
          search.strongest = step
          search.strongestContrast = lowest
        }
      }
    }
    return open
  }

  if (measure(start) > 0) {
    val nearestFirst =
      (0..LIGHTNESS_STEPS).sortedBy { abs(it.toDouble() / LIGHTNESS_STEPS - base.l) }
    for (index in nearestFirst) {
      if (measure(base.atLightness(index.toDouble() / LIGHTNESS_STEPS)) == 0) break
    }
  }
  return searches.map { it.found ?: it.strongest }
}

private fun hoverFill(fill: Color, contrast: Color): Color {
  val color = fill.toOklch()
  val direction = if (contrast == Color.White) -1 else 1
  val away = color.atLightness(color.l + direction * HOVER_STEP)
  return if (away == fill) color.atLightness(color.l - direction * HOVER_STEP) else away
}

private fun contrast(a: Color, b: Color): Double {
  val first = a.luminance()
  val second = b.luminance()
  return (max(first, second) + 0.05) / (min(first, second) + 0.05)
}

private fun Color.luminance(): Double {
  val (r, g, b) = toLinear()
  return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

private fun Color.opaque(): Color = copy(alpha = 1f)

// Mixes in oklab, as culori's interpolate does
private fun mix(from: Color, to: Color, weight: Double): Color {
  val a = from.toOklab()
  val b = to.toOklab()
  return Oklab(
      l = a.l + (b.l - a.l) * weight,
      a = a.a + (b.a - a.a) * weight,
      b = a.b + (b.b - a.b) * weight,
    )
    .toRgb()
    .toColor()
}

private data class Rgb(val r: Double, val g: Double, val b: Double) {
  val displayable: Boolean
    get() = r in 0.0..1.0 && g in 0.0..1.0 && b in 0.0..1.0

  fun toColor(): Color = Color(red = r.toChannel(), green = g.toChannel(), blue = b.toChannel())

  private fun Double.toChannel(): Int = (coerceIn(0.0, 1.0) * 255).roundToInt()
}

private data class Oklab(val l: Double, val a: Double, val b: Double) {
  fun toRgb(): Rgb {
    val lms1 = (l + 0.3963377773761749 * a + 0.2158037573099136 * b).pow(3)
    val lms2 = (l - 0.1055613458156586 * a - 0.0638541728258133 * b).pow(3)
    val lms3 = (l - 0.0894841775298119 * a - 1.2914855480194092 * b).pow(3)
    return Rgb(
      r =
        toGamma(4.0767416360759574 * lms1 - 3.3077115392580616 * lms2 + 0.2309699031821044 * lms3),
      g =
        toGamma(-1.2684379732850317 * lms1 + 2.6097573492876887 * lms2 - 0.3413193760026573 * lms3),
      b =
        toGamma(-0.0041960761386756 * lms1 - 0.7034186179359362 * lms2 + 1.7076146940746117 * lms3),
    )
  }
}

private data class Oklch(val l: Double, val c: Double, val h: Double) {
  fun toRgb(): Rgb {
    val radians = h / 180 * PI
    val a = if (c != 0.0) c * cos(radians) else 0.0
    val b = if (c != 0.0) c * sin(radians) else 0.0
    return Oklab(l, a, b).toRgb()
  }

  // culori's clampChroma: keeps lightness and hue, and bisects for the strongest chroma in sRGB
  fun atLightness(lightness: Double): Color {
    val color = copy(l = lightness.coerceIn(0.0, 1.0))
    val rgb = color.toRgb()
    if (rgb.displayable) return rgb.toColor()
    val grey = color.copy(c = 0.0)
    if (!grey.toRgb().displayable) return grey.toRgb().toColor()
    var start = 0.0
    var end = color.c
    var clamped = grey
    var lastGood = 0.0
    while (end - start > CHROMA_RESOLUTION) {
      clamped = clamped.copy(c = start + (end - start) * 0.5)
      if (clamped.toRgb().displayable) {
        lastGood = clamped.c
        start = clamped.c
      } else {
        end = clamped.c
      }
    }
    val result = if (clamped.toRgb().displayable) clamped else clamped.copy(c = lastGood)
    return result.toRgb().toColor()
  }
}

private fun Color.toLinear(): Triple<Double, Double, Double> =
  Triple(toLinear(red.byte()), toLinear(green.byte()), toLinear(blue.byte()))

// Colours come from hex, so read each channel back as its exact byte
private fun Float.byte(): Double = (this * 255).roundToInt() / 255.0

private fun Color.toOklab(): Oklab {
  val (r, g, b) = toLinear()
  val lms1 = cbrt(0.412221469470763 * r + 0.5363325372617348 * g + 0.0514459932675022 * b)
  val lms2 = cbrt(0.2119034958178252 * r + 0.6806995506452344 * g + 0.1073969535369406 * b)
  val lms3 = cbrt(0.0883024591900564 * r + 0.2817188391361215 * g + 0.6299787016738222 * b)
  return Oklab(
    l = 0.210454268309314 * lms1 + 0.7936177747023054 * lms2 - 0.0040720430116193 * lms3,
    a = 1.9779985324311684 * lms1 - 2.4285922420485799 * lms2 + 0.450593709617411 * lms3,
    b = 0.0259040424655478 * lms1 + 0.7827717124575296 * lms2 - 0.8086757549230774 * lms3,
  )
}

private fun Color.toOklch(): Oklch {
  val (l, a, b) = toOklab()
  val c = sqrt(a * a + b * b)
  val h = if (c != 0.0) (atan2(b, a) * 180 / PI).mod(360.0) else 0.0
  return Oklch(l, c, h)
}

private fun toLinear(c: Double): Double {
  val abs = abs(c)
  return if (abs <= 0.04045) c / 12.92
  else (sign(c).takeIf { it != 0.0 } ?: 1.0) * ((abs + 0.055) / 1.055).pow(2.4)
}

private fun toGamma(c: Double): Double {
  val abs = abs(c)
  return if (abs > 0.0031308) {
    (sign(c).takeIf { it != 0.0 } ?: 1.0) * (1.055 * abs.pow(1 / 2.4) - 0.055)
  } else {
    c * 12.92
  }
}
