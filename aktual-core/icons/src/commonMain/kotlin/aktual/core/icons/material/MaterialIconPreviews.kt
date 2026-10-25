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
      AccountBalance,
      AccountBalanceWallet,
      Add,
      Apps,
      ArrowBack,
      ArrowDownward,
      ArrowDropDown,
      ArrowRight,
      ArrowUpward,
      Badge,
      BarChart,
      Block,
      Bookmark,
      BlurCircular,
      BlurOn,
      Brightness2,
      Brightness3,
      Build,
      CalendarToday,
      CalendarViewWeek,
      Check,
      ChevronRight,
      Clear,
      ClearAll,
      Cloud,
      ContentCopy,
      CurrencyPound,
      DarkMode,
      DecimalDecrease,
      DecimalIncrease,
      Deselect,
      Delete,
      DeleteForever,
      Description,
      Dialogs,
      Edit,
      Error,
      ExpandMore,
      FilterList,
      Fingerprint,
      FormatAlignCenter,
      FormatAlignLeft,
      FormatAlignRight,
      FormatBold,
      FormatItalic,
      FormatListBulleted,
      Home,
      Info,
      Key,
      LightMode,
      Link,
      Lock,
      Logout,
      LineEndArrowNotch,
      LineStartArrowNotch,
      Menu,
      Merge,
      MoreVert,
      Numbers,
      OfflinePin,
      Opacity,
      OpenInNew,
      Pause,
      PlayArrow,
      PostAdd,
      PushPin,
      ReceiptLong,
      Refresh,
      Save,
      SaveAs,
      Search,
      SearchOff,
      Security,
      SelectAll,
      Settings,
      Share,
      Shuffle,
      Sort,
      SpaceBar,
      Speed125,
      SwapHoriz,
      Sync,
      ThemeRoutine,
      Timer,
      Title,
      Tune,
      Undo,
      Visibility,
      VisibilityOff,
      Warning,
      ZoomOut,
    )
  }
