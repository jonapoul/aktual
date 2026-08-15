package aktual.core.ui

import aktual.core.l10n.Res
import aktual.core.l10n.inter_black
import aktual.core.l10n.inter_bold
import aktual.core.l10n.inter_extrabold
import aktual.core.l10n.inter_extralight
import aktual.core.l10n.inter_light
import aktual.core.l10n.inter_medium
import aktual.core.l10n.inter_regular
import aktual.core.l10n.inter_semibold
import aktual.core.l10n.inter_thin
import aktual.core.theme.Colors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize.Min
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.persistentMapOf
import org.jetbrains.compose.resources.Font

@Composable
internal fun aktualTypography(colors: Colors = AktualTheme.colors): Typography {
  val font =
    FontFamily(
      Font(Res.font.inter_thin, W100),
      Font(Res.font.inter_extralight, W200),
      Font(Res.font.inter_light, W300),
      Font(Res.font.inter_regular, W400),
      Font(Res.font.inter_medium, W500),
      Font(Res.font.inter_semibold, W600),
      Font(Res.font.inter_bold, W700),
      Font(Res.font.inter_extrabold, W800),
      Font(Res.font.inter_black, W900),
    )

  return with(font) {
    Typography(
      displayLarge = textStyle(weight = W700, size = 40.sp),
      displayMedium = textStyle(weight = W600, size = 35.sp),
      displaySmall = textStyle(weight = W500, size = 30.sp),
      headlineLarge = textStyle(weight = W700, size = 30.sp, color = colors.pageTextPositive),
      headlineMedium = textStyle(weight = W600, size = 25.sp, color = colors.pageTextPositive),
      headlineSmall = textStyle(weight = W500, size = 20.sp, color = colors.pageTextPositive),
      titleLarge = textStyle(weight = W500, size = 25.sp),
      titleMedium = textStyle(weight = W400, size = 23.sp),
      titleSmall = textStyle(weight = W300, size = 22.sp),
      bodyLarge = textStyle(size = 16.sp, height = 22.4.sp),
      bodyMedium = textStyle(size = 15.sp, height = 21.4.sp),
      bodySmall = textStyle(size = 14.sp, height = 20.4.sp),
      labelLarge = textStyle(size = 14.sp, color = colors.pageTextSubdued),
      labelMedium = textStyle(size = 13.sp, color = colors.pageTextSubdued),
      labelSmall = textStyle(size = 12.sp, color = colors.pageTextSubdued),
    )
  }
}

private fun FontFamily.textStyle(
  weight: FontWeight? = null,
  size: TextUnit = TextUnit.Unspecified,
  color: Color = Color.Unspecified,
  height: TextUnit = TextUnit.Unspecified,
): TextStyle =
  TextStyle(
    fontSize = size,
    fontWeight = weight,
    fontFamily = this,
    color = color,
    lineHeight = height,
  )

@Preview(widthDp = 1200)
@Composable
private fun PreviewTypography(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column {
      for ((name, style) in styles()) {
        Row(
          modifier = Modifier.height(Min),
          verticalAlignment = CenterVertically,
        ) {
          Box(
            modifier = Modifier.fillMaxHeight().width(150.dp),
            contentAlignment = Center,
          ) {
            Text(modifier = Modifier.fillMaxWidth(), text = name, textAlign = Start)
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
private fun styles() =
  with(aktualTypography()) {
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
