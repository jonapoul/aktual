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
private fun PreviewAktualIcons(
    @PreviewParameter(AktualIconsProvider::class) icon: ImageVector,
) = MaterialTheme {
  Icon(
      imageVector = icon,
      contentDescription = null,
      tint = Color.White,
  )
}

private class AktualIconsProvider : CollectionPreviewParameterProvider<ImageVector>(materialIcons) {
  override fun getDisplayName(index: Int) = materialIcons[index].name
}

private val materialIcons =
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
    }

@Preview
@Composable
private fun PreviewMaterial(
    @PreviewParameter(MaterialIconsProvider::class) icon: ImageVector,
) = MaterialTheme {
  Icon(
      imageVector = icon,
      contentDescription = null,
      tint = Color.White,
  )
}

private class MaterialIconsProvider : CollectionPreviewParameterProvider<ImageVector>(aktualIcons) {
  override fun getDisplayName(index: Int) = aktualIcons[index].name
}

private val aktualIcons =
    with(MaterialIcons) {
      listOf(
          Add,
          Apps,
          ArrowBack,
          BarChart,
          Block,
          Brightness2,
          Brightness3,
          CalendarToday,
          Check,
          Clear,
          Cloud,
          Delete,
          DeleteForever,
          Edit,
          Error,
          Info,
          Key,
          LightMode,
          MoreVert,
          Numbers,
          Refresh,
          Search,
          SearchOff,
          Settings,
          Sync,
          Timer,
          Visibility,
          VisibilityOff,
          Warning,
      )
    }
