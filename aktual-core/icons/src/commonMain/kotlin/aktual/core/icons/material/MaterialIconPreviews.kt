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
      BarChart,
      Block,
      BlurOn,
      Brightness2,
      Brightness3,
      CalendarToday,
      CalendarViewWeek,
      Check,
      Clear,
      Cloud,
      CurrencyPound,
      DarkMode,
      DecimalDecrease,
      DecimalIncrease,
      Delete,
      DeleteForever,
      Edit,
      Error,
      Info,
      Key,
      LinearScale,
      LineEndArrowNotch,
      LineStartArrowNotch,
      MoreVert,
      Numbers,
      OpenInNew,
      Refresh,
      Search,
      SearchOff,
      Settings,
      SpaceBar,
      Speed125,
      Sync,
      ThemeRoutine,
      Timer,
      TransitionDissolve,
      Visibility,
      VisibilityOff,
      Warning,
    )
  }
