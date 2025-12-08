package aktual.core.ui

import aktual.core.model.ColorSchemeType
import aktual.l10n.Res
import aktual.l10n.inter_black
import aktual.l10n.inter_bold
import aktual.l10n.inter_extrabold
import aktual.l10n.inter_extralight
import aktual.l10n.inter_light
import aktual.l10n.inter_medium
import aktual.l10n.inter_regular
import aktual.l10n.inter_semibold
import aktual.l10n.inter_thin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.persistentMapOf
import org.jetbrains.compose.resources.Font

val AktualTypography: Typography
  @Composable
  @ReadOnlyComposable
  get() = MaterialTheme.typography

@Composable
internal fun aktualTypography(theme: Theme = LocalTheme.current): Typography {
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

@Preview(widthDp = 1200)
@Composable
private fun PreviewTypography(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  Column {
    for ((name, style) in styles()) {
      Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .fillMaxHeight()
            .width(150.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            modifier = Modifier.fillMaxWidth(),
            text = name,
            textAlign = TextAlign.Start,
          )
        }

        Text(
          modifier = Modifier.weight(1f),
          text = "Quick brown fox? Jumped over the lazy dog!",
          style = style,
        )
      }
    }
  }
}

@Composable
private fun styles() = with(aktualTypography()) {
  persistentMapOf(
    "displayLarge" to displayLarge,
    "displayMedium" to displayMedium,
    "displaySmall" to displaySmall,
    "headlineLarge" to headlineLarge,
    "headlineMedium" to headlineMedium,
    "headlineSmall" to headlineSmall,
    "titleLarge" to titleLarge,
    "titleMedium" to titleMedium,
    "titleSmall" to titleSmall,
    "bodyLarge" to bodyLarge,
    "bodyMedium" to bodyMedium,
    "bodySmall" to bodySmall,
    "labelLarge" to labelLarge,
    "labelMedium" to labelMedium,
    "labelSmall" to labelSmall,
  )
}
