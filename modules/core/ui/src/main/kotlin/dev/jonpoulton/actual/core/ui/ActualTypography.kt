package dev.jonpoulton.actual.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jonpoulton.actual.core.res.R as ResR

@Composable
@ReadOnlyComposable
fun actualTypography(colors: ActualColorScheme = LocalActualColorScheme.current): Typography {
  return Typography(
    displayLarge = textStyle(FontWeight.W700, fontSize = 30.sp),
    displayMedium = textStyle(FontWeight.W600, fontSize = 25.sp),
    displaySmall = textStyle(FontWeight.W500, fontSize = 20.sp),
    headlineLarge = textStyle(FontWeight.W700, colors.pageTextPositive, 30.sp),
//     headlineMedium =
//     headlineSmall =
//     titleLarge =
//     titleMedium =
//     titleSmall =
    bodyLarge = textStyle(fontWeight = null, colors.pageText, fontSize = 16.sp, lineHeight = 24.sp),
//     bodyMedium =
//     bodySmall =
//     labelLarge =
    labelMedium = textStyle(fontWeight = null, colors.pageTextSubdued, fontSize = 13.sp),
//     labelSmall =
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

private fun textStyle(
  fontWeight: FontWeight?,
  color: Color = Color.Unspecified,
  fontSize: TextUnit = TextUnit.Unspecified,
  lineHeight: TextUnit = TextUnit.Unspecified,
): TextStyle = TextStyle(
  fontSize = fontSize,
  fontWeight = fontWeight,
  fontFamily = ActualFontFamily,
  color = color,
  lineHeight = lineHeight,
)

@ActualScreenPreview
@Composable
private fun PreviewTypography() {
  PreviewActualScreen {
    val styles = with(actualTypography()) {
      listOf(
        displayLarge,
        displayMedium,
        displaySmall,
        headlineLarge,
//         headlineMedium,
//         headlineSmall,
//         titleLarge,
//         titleMedium,
//         titleSmall,
        bodyLarge,
//         bodyMedium,
//         bodySmall,
//         labelLarge,
        labelMedium,
//         labelSmall,
      )
    }
    Column {
      styles.forEach { style ->
        Text(
          modifier = Modifier.padding(8.dp),
          text = "Quick brown fox? Jumped over the lazy dog!",
          style = style,
        )
      }
    }
  }
}
