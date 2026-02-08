package aktual.core.icons

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider

@Preview
@Composable
private fun PreviewIcons(
  @PreviewParameter(IconPreviewParameters::class) icon: ImageVector,
) = MaterialTheme {
  Icon(
    imageVector = icon,
    contentDescription = null,
  )
}

private class IconPreviewParameters : CollectionPreviewParameterProvider<ImageVector>(
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
    )
  },
)
