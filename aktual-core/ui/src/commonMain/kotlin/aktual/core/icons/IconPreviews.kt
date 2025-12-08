package aktual.core.icons

import aktual.core.model.ColorSchemeType
import aktual.core.ui.PreviewParameters
import aktual.core.ui.PreviewWithColorScheme
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Preview
@Composable
private fun PreviewIcons(
  @PreviewParameter(IconPreviewParameters::class) params: Pair<ColorSchemeType, ImageVector>,
) = PreviewWithColorScheme(params.first) {
  Icon(
    imageVector = params.second,
    contentDescription = null,
  )
}

private class IconPreviewParameters : PreviewParameters<Pair<ColorSchemeType, ImageVector>>(
  with(AktualIcons) {
    listOf(
      ArrowThickDown,
      ArrowThickUp,
      CloseBracket,
      Cloud,
      CloudCheck,
      CloudDownload,
      CloudUnknown,
      CloudUpload,
      CloudWarning,
      Equals,
      FileDouble,
      Key,
      OpenBracket,
      Sum,
    ).flatMap { icon ->
      ColorSchemeType.entries.map { type ->
        type to icon
      }
    }
  },
)
