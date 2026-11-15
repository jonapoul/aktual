package aktual.prefs.components

import aktual.prefs.AppIcon
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Draws an app icon the way a launcher shows it: the middle 72 units of the 108-unit adaptive icon
// canvas. Keep these in sync with the vector drawables in aktual-app/android/src/main/res/drawable.
@Composable
fun AppIconImage(icon: AppIcon, modifier: Modifier = Modifier, shape: Shape = AppIconShape) {
  Box(modifier.clip(shape).drawBehind { drawAppIcon(icon) })
}

// Roughly the squircle most launchers mask to
val AppIconShape: Shape = RoundedCornerShape(percent = 30)

@Composable
fun rememberAppIconPainter(icon: AppIcon, cornerFraction: Float = 0f): Painter =
  remember(icon, cornerFraction) { AppIconPainter(icon, cornerFraction) }

private class AppIconPainter(private val icon: AppIcon, private val cornerFraction: Float) :
  Painter() {
  override val intrinsicSize: Size = Unspecified

  override fun DrawScope.onDraw() {
    if (cornerFraction <= 0f) {
      drawAppIcon(icon)
    } else {
      val radius = size.minDimension * cornerFraction
      val clip =
        Path().apply {
          addRoundRect(
            RoundRect(
              left = 0f,
              top = 0f,
              right = size.width,
              bottom = size.height,
              cornerRadius = CornerRadius(radius),
            ),
          )
        }
      clipPath(clip) { drawAppIcon(icon) }
    }
  }
}

fun DrawScope.drawAppIcon(icon: AppIcon) {
  val scale = size.minDimension / VIEWPORT
  withTransform(
    transformBlock = {
      scale(scale, scale, pivot = Zero)
      translate(-INSET, -INSET)
    },
  ) {
    drawRect(icon.background(), topLeft = Zero, size = Size(CANVAS, CANVAS))
    drawPath(LetterA, icon.foreground())
  }
}

private fun AppIcon.background(): Brush =
  when (this) {
    Default -> CornerGradient
    Light -> LightBackground
    Dark -> DarkBackground
    Violet -> VioletBackground
    Sunset -> SunsetGradient
    Aurora -> AuroraGradient
    Ink -> WhiteBrush
  }

private fun AppIcon.foreground(): Brush =
  when (this) {
    Light,
    Dark -> LetterGradient
    Ink -> InkLetter
    Default,
    Violet,
    Sunset,
    Aurora -> WhiteBrush
  }

private val WhiteBrush = SolidColor(White)
private val LightBackground = SolidColor(Color(0xFFF7F3FF))
private val DarkBackground = SolidColor(Color(0xFF14111C))
private val VioletBackground = SolidColor(Color(0xFF7B2FF7))
private val InkLetter = SolidColor(Color(0xFF1B1726))

private val SunsetGradient =
  Brush.linearGradient(
    colors = listOf(Color(0xFFFFB443), Color(0xFFFF4F7B)),
    start = Zero,
    end = Offset(CANVAS, CANVAS),
  )

private val AuroraGradient =
  Brush.linearGradient(
    colors = listOf(Color(0xFF12C2A9), Color(0xFF7B2FF7)),
    start = Offset(0f, CANVAS),
    end = Offset(CANVAS, 0f),
  )

private val GradientStops =
  arrayOf(0f to Color(0xFFEF5A4C), 0.45f to Color(0xFFC216D6), 1f to Color(0xFF7B2FF7))

private val CornerGradient =
  Brush.radialGradient(colorStops = GradientStops, center = Offset(CANVAS, CANVAS), radius = 150f)

private val LetterGradient =
  Brush.radialGradient(colorStops = GradientStops, center = Offset(75.6f, 76.32f), radius = 60f)

// A triangle with a notch cut from its base, fitted inside the adaptive icon's safe zone
private val LetterA =
  Path().apply {
    moveTo(54f, 31.68f)
    lineTo(75.6f, 76.32f)
    lineTo(64.08f, 76.32f)
    lineTo(54f, 56.16f)
    lineTo(43.92f, 76.32f)
    lineTo(32.4f, 76.32f)
    close()
  }

private const val CANVAS = 108f
private const val VIEWPORT = 72f
private const val INSET = (CANVAS - VIEWPORT) / 2

@Preview
@Composable
private fun PreviewAppIcons() = Column {
  FlowRow {
    AppIcon.entries.forEach { AppIconImage(it, Modifier.size(PreviewSize)) }
  }
  FlowRow {
    AppIcon.entries.forEach { AppIconImage(it, Modifier.size(PreviewSize), CircleShape) }
  }
}

private val PreviewSize: Dp = 64.dp
