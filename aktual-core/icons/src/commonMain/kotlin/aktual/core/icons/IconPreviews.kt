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
private fun PreviewAktualIcons(@PreviewParameter(AktualIconsProvider::class) icon: ImageVector) =
  MaterialTheme {
    Icon(imageVector = icon, contentDescription = null, tint = White)
  }

private class AktualIconsProvider : CollectionPreviewParameterProvider<ImageVector>(aktualIcons) {
  override fun getDisplayName(index: Int) = aktualIcons[index].name.removePrefix("Aktual.")
}

private val aktualIcons =
  with(AktualIcons) {
    listOf(
      Add,
      AlertTriangle,
      ArrowsSynchronize,
      ArrowThickDown,
      ArrowThickUp,
      Bookmark,
      Calendar3,
      Check,
      CheveronDown,
      CheveronLeft,
      CheveronRight,
      CloseBracket,
      Cloud,
      CloudCheck,
      CloudDownload,
      CloudUnknown,
      CloudWarning,
      Cog,
      Copy,
      Delete,
      DotsHorizontalTriple,
      Equals,
      ExclamationOutline,
      FileDouble,
      Git,
      InformationOutline,
      Key,
      LeftArrow2,
      Library,
      LightBulb,
      LockClosed,
      Merge,
      MoonStars,
      OpenBracket,
      Pencil1,
      Pin,
      Refresh,
      Reports,
      RightArrow2,
      SearchAlternate,
      Split,
      Subtract,
      Sum,
      Sun,
      System,
      Tag,
      Trash,
      Tuning,
      UserGroup,
      ViewHide,
      ViewShow,
      Wallet,
    )
  }
