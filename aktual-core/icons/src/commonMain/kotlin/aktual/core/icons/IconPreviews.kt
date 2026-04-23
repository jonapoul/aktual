package aktual.core.icons

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider

@Preview
@Composable
private fun PreviewAktualIcons(@PreviewParameter(AktualIconsProvider::class) icon: ImageVector) =
  MaterialTheme {
    Icon(imageVector = icon, contentDescription = null, tint = Color.White)
  }

private class AktualIconsProvider : CollectionPreviewParameterProvider<ImageVector>(aktualIcons) {
  override fun getDisplayName(index: Int) = aktualIcons[index].name.removePrefix("Aktual.")
}

private val aktualIcons =
  with(AktualIcons) {
    listOf(
      ArrowThickDown,
      ArrowThickUp,
      ChevronUp,
      CloseBracket,
      Cloud,
      CloudCheck,
      CloudDownload,
      CloudUnknown,
      CloudUpload,
      CloudWarning,
      Equals,
      FileDouble,
      Git,
      Key,
      OpenBracket,
      Reports,
      Sum,
      Tuning,
    )
  }
