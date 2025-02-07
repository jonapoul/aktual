package actual.core.ui

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
import actual.core.res.R as CoreR

@Composable
@ReadOnlyComposable
fun actualTypography(theme: Theme = LocalTheme.current): Typography {
  /**
   * WARNING: DON'T SET A COLOUR FOR bodyLarge - IT'LL OVERRIDE THOSE SET IN TEXT FIELDS
   */
  return Typography(
    displayLarge = actualTextStyle(fontWeight = FontWeight.W700, fontSize = 30.sp),
    displayMedium = actualTextStyle(fontWeight = FontWeight.W600, fontSize = 25.sp),
    displaySmall = actualTextStyle(fontWeight = FontWeight.W500, fontSize = 20.sp),
    headlineLarge = actualTextStyle(fontWeight = FontWeight.W700, color = theme.pageTextPositive, fontSize = 30.sp),
    headlineMedium = actualTextStyle(fontWeight = FontWeight.W600, color = theme.pageText, fontSize = 25.sp),
//     headlineSmall =
//     titleLarge =
//     titleMedium =
//     titleSmall =
    bodyLarge = actualTextStyle(fontSize = 16.sp, lineHeight = 22.4.sp),
    bodyMedium = actualTextStyle(fontSize = 15.sp, lineHeight = 21.4.sp),
    bodySmall = actualTextStyle(fontSize = 14.sp, lineHeight = 20.4.sp),
//     labelLarge =
    labelMedium = actualTextStyle(color = theme.pageTextSubdued, fontSize = 13.sp),
//     labelSmall =
  )
}

val ActualFontFamily = FontFamily(
  Font(CoreR.font.inter_thin, FontWeight.W100),
  Font(CoreR.font.inter_extralight, FontWeight.W200),
  Font(CoreR.font.inter_light, FontWeight.W300),
  Font(CoreR.font.inter_regular, FontWeight.W400),
  Font(CoreR.font.inter_medium, FontWeight.W500),
  Font(CoreR.font.inter_semibold, FontWeight.W600),
  Font(CoreR.font.inter_bold, FontWeight.W700),
  Font(CoreR.font.inter_extrabold, FontWeight.W800),
  Font(CoreR.font.inter_black, FontWeight.W900),
)

fun actualTextStyle(
  fontWeight: FontWeight? = null,
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
        headlineMedium,
//         headlineSmall,
//         titleLarge,
//         titleMedium,
//         titleSmall,
        bodyLarge,
        bodyMedium,
        bodySmall,
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
