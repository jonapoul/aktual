package aktual.core.icons.material

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
private fun PreviewMaterial(@PreviewParameter(MaterialIconsProvider::class) icon: ImageVector) =
  MaterialTheme {
    Icon(imageVector = icon, contentDescription = null, tint = Color.White)
  }

private class MaterialIconsProvider :
  CollectionPreviewParameterProvider<ImageVector>(materialIcons) {
  override fun getDisplayName(index: Int) = materialIcons[index].name.removePrefix("Material.")
}

private val materialIcons =
  with(MaterialIcons) {
    listOf(
      Add,
      Apps,
      ArrowBack,
      ArrowRight,
      Badge,
      BarChart,
      Block,
      BlurOn,
      Brightness2,
      Brightness3,
      Build,
      CalendarToday,
      CalendarViewWeek,
      Check,
      Clear,
      ClearAll,
      Cloud,
      CurrencyPound,
      DarkMode,
      DecimalDecrease,
      DecimalIncrease,
      Deselect,
      Delete,
      DeleteForever,
      Dialogs,
      Edit,
      Error,
      FilterList,
      Info,
      Key,
      LightMode,
      LinearScale,
      Logout,
      LineEndArrowNotch,
      LineStartArrowNotch,
      Menu,
      MoreVert,
      Numbers,
      OfflinePin,
      OpenInNew,
      Refresh,
      Search,
      SearchOff,
      SelectAll,
      Settings,
      Sort,
      SpaceBar,
      Speed125,
      SwapHoriz,
      Sync,
      ThemeRoutine,
      Timer,
      TransitionDissolve,
      Visibility,
      VisibilityOff,
      Warning,
    )
  }
