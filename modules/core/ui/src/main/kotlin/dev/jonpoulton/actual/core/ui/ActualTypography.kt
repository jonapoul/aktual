package dev.jonpoulton.actual.core.ui

import alakazam.android.ui.compose.PreviewThemes
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import dev.jonpoulton.actual.core.res.R as ResR

// TODO: Create custom text styles here
val ActualTypography: Typography
  @Composable
  @ReadOnlyComposable
  get() {
    val colors = MaterialTheme.colorScheme
    return Typography(
      displayLarge = ActualFontFamily.textStyle(colors.primary, FontWeight.W700, fontSize = 30.sp),
      displayMedium = ActualFontFamily.textStyle(colors.primary, FontWeight.W600, fontSize = 25.sp),
      displaySmall = ActualFontFamily.textStyle(colors.primary, FontWeight.W500, fontSize = 20.sp),
// headlineLarge = ActualFontFamily.textStyle(ActualColors., FontWeight.W, fontSize = .sp),
// headlineMedium = ActualFontFamily.textStyle(ActualColors., FontWeight.W, fontSize = .sp),
// headlineSmall = ActualFontFamily.textStyle(ActualColors., FontWeight.W, fontSize = .sp),
// titleLarge = ActualFontFamily.textStyle(ActualColors., FontWeight.W, fontSize = .sp),
// titleMedium = ActualFontFamily.textStyle(ActualColors., FontWeight.W, fontSize = .sp),
// titleSmall = ActualFontFamily.textStyle(ActualColors., FontWeight.W, fontSize = .sp),
// bodyLarge = ActualFontFamily.textStyle(ActualColors., FontWeight.W, fontSize = .sp),
// bodyMedium = ActualFontFamily.textStyle(ActualColors., FontWeight.W, fontSize = .sp),
// bodySmall = ActualFontFamily.textStyle(ActualColors., FontWeight.W, fontSize = .sp),
// labelLarge = ActualFontFamily.textStyle(ActualColors., FontWeight.W, fontSize = .sp),
// labelMedium = ActualFontFamily.textStyle(ActualColors., FontWeight.W, fontSize = .sp),
// labelSmall = ActualFontFamily.textStyle(ActualColors., FontWeight.W, fontSize = .sp),
    )
  }

val ActualFontFamily = FontFamily(
  Font(ResR.font.inter_thin, FontWeight.W100),
  Font(ResR.font.inter_extralight, FontWeight.W200),
  Font(ResR.font.inter_light, FontWeight.W300),
  Font(ResR.font.inter_regular, FontWeight.W400),
  Font(ResR.font.inter_medium, FontWeight.W500),
  Font(ResR.font.inter_semibold, FontWeight.W600),
  Font(ResR.font.inter_bold, FontWeight.W700),
  Font(ResR.font.inter_extrabold, FontWeight.W800),
  Font(ResR.font.inter_black, FontWeight.W900),
)

private fun FontFamily.textStyle(color: Color, fontWeight: FontWeight, fontSize: TextUnit): TextStyle =
  TextStyle(
    color = color,
    fontSize = fontSize,
    fontWeight = fontWeight,
    fontFamily = this,
  )

@PreviewThemes
@Composable
private fun PreviewTypography() = ActualTheme {
  val width: Dp
  val height: Dp
  with(LocalDensity.current) {
    width = 1080.toDp()
    height = 2280.toDp()
  }
  Surface(modifier = Modifier.size(width, height)) {
    val typography = ActualTypography
    val styles = listOf(
      typography.displayLarge,
      typography.displayMedium,
      typography.displaySmall,
      typography.headlineLarge,
      typography.headlineMedium,
      typography.headlineSmall,
      typography.titleLarge,
      typography.titleMedium,
      typography.titleSmall,
      typography.bodyLarge,
      typography.bodyMedium,
      typography.bodySmall,
      typography.labelLarge,
      typography.labelMedium,
      typography.labelSmall,
    )
    LazyColumn {
      items(styles) { style ->
        Text(text = "Quick brown fox ${style.color}", style = style)
      }
    }
  }
}
