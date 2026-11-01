package aktual.core.icons.material

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider

@Preview
@Composable
private fun PreviewMaterial(@PreviewParameter(MaterialIconsProvider::class) icon: ImageVector) =
  MaterialTheme {
    Icon(imageVector = icon, contentDescription = null, tint = White)
  }

private class MaterialIconsProvider :
  CollectionPreviewParameterProvider<ImageVector>(materialIcons) {
  override fun getDisplayName(index: Int) = materialIcons[index].name.removePrefix("Material.")
}

private val materialIcons =
  with(MaterialIcons) {
    listOf(
      Apps,
      ArrowDownward,
      ArrowUpward,
      Badge,
      BlurCircular,
      BlurOn,
      Build,
      CalendarViewWeek,
      CurrencyPound,
      DecimalDecrease,
      DecimalIncrease,
      DeleteForever,
      Description,
      Dialogs,
      Fingerprint,
      FormatAlignCenter,
      FormatAlignLeft,
      FormatAlignRight,
      FormatBold,
      FormatItalic,
      FormatListBulleted,
      Home,
      Link,
      Logout,
      LineEndArrowNotch,
      LineStartArrowNotch,
      Menu,
      Numbers,
      OfflinePin,
      Opacity,
      OpenInNew,
      Pause,
      PlayArrow,
      PostAdd,
      ReceiptLong,
      RoundedCorner,
      Save,
      SaveAs,
      SearchOff,
      Security,
      Share,
      Shuffle,
      Sort,
      SpaceBar,
      Speed125,
      SubdirectoryArrowRight,
      SwapHoriz,
      Texture,
      Timer,
      Title,
      Undo,
      ZoomOut,
    )
  }
