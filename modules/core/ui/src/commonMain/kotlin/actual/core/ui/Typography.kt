package actual.core.ui

import actual.l10n.Res
import actual.l10n.inter_black
import actual.l10n.inter_bold
import actual.l10n.inter_extrabold
import actual.l10n.inter_extralight
import actual.l10n.inter_light
import actual.l10n.inter_medium
import actual.l10n.inter_regular
import actual.l10n.inter_semibold
import actual.l10n.inter_thin
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font

val ActualTypography: Typography
  @Composable
  @ReadOnlyComposable
  get() = MaterialTheme.typography

@Composable
internal fun actualTypography(theme: Theme = LocalTheme.current): Typography {
  val font = FontFamily(
    Font(Res.font.inter_thin, FontWeight.W100),
    Font(Res.font.inter_extralight, FontWeight.W200),
    Font(Res.font.inter_light, FontWeight.W300),
    Font(Res.font.inter_regular, FontWeight.W400),
    Font(Res.font.inter_medium, FontWeight.W500),
    Font(Res.font.inter_semibold, FontWeight.W600),
    Font(Res.font.inter_bold, FontWeight.W700),
    Font(Res.font.inter_extrabold, FontWeight.W800),
    Font(Res.font.inter_black, FontWeight.W900),
  )

  return with(font) {
    Typography(
      displayLarge = textStyle(weight = FontWeight.W700, size = 40.sp),
      displayMedium = textStyle(weight = FontWeight.W600, size = 35.sp),
      displaySmall = textStyle(weight = FontWeight.W500, size = 30.sp),
      headlineLarge = textStyle(weight = FontWeight.W700, size = 30.sp, color = theme.pageTextPositive),
      headlineMedium = textStyle(weight = FontWeight.W600, size = 25.sp, color = theme.pageTextPositive),
      headlineSmall = textStyle(weight = FontWeight.W500, size = 20.sp, color = theme.pageTextPositive),
      titleLarge = textStyle(weight = FontWeight.W500, size = 25.sp),
      titleMedium = textStyle(weight = FontWeight.W400, size = 23.sp),
      titleSmall = textStyle(weight = FontWeight.W300, size = 22.sp),
      bodyLarge = textStyle(size = 16.sp, height = 22.4.sp),
      bodyMedium = textStyle(size = 15.sp, height = 21.4.sp),
      bodySmall = textStyle(size = 14.sp, height = 20.4.sp),
      labelLarge = textStyle(size = 14.sp, color = theme.pageTextSubdued),
      labelMedium = textStyle(size = 13.sp, color = theme.pageTextSubdued),
      labelSmall = textStyle(size = 12.sp, color = theme.pageTextSubdued),
    )
  }
}

private fun FontFamily.textStyle(
  weight: FontWeight? = null,
  size: TextUnit = TextUnit.Unspecified,
  color: Color = Color.Unspecified,
  height: TextUnit = TextUnit.Unspecified,
): TextStyle = TextStyle(
  fontSize = size,
  fontWeight = weight,
  fontFamily = this,
  color = color,
  lineHeight = height,
)
