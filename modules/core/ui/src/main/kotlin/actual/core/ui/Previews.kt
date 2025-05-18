@file:Suppress("ModifierNotUsedAtRoot")

package actual.core.ui

import actual.core.model.ColorSchemeType
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf

@Composable
fun PreviewColumn(
  modifier: Modifier = Modifier,
  content: @Composable (ColorSchemeType) -> Unit,
) {
  LazyColumn {
    items(SchemeTypes, key = { it }) { schemeType ->
      Box(modifier = modifier) {
        PreviewWithColorScheme(schemeType, content = content)
      }
    }
  }
}

@Composable
fun PreviewActualRow(
  modifier: Modifier = Modifier,
  content: @Composable (ColorSchemeType) -> Unit,
) {
  LazyRow {
    items(SchemeTypes, key = { it }) { schemeType ->
      Box(modifier = modifier) {
        PreviewWithColorScheme(schemeType, content = content)
      }
    }
  }
}

@Composable
fun PreviewScreen(
  modifier: Modifier = Modifier,
  content: @Composable (ColorSchemeType) -> Unit,
) {
  LazyRow {
    items(SchemeTypes, key = { it }) { schemeType ->
      PreviewWithColorScheme(
        modifier = modifier
          .width(MY_PHONE_WIDTH_DP.dp)
          .height(MY_PHONE_HEIGHT_DP.dp),
        schemeType = schemeType,
        content = { content(schemeType) },
      )
    }
  }
}

@Composable
fun PreviewWithColorScheme(
  schemeType: ColorSchemeType,
  modifier: Modifier = Modifier,
  content: @Composable (ColorSchemeType) -> Unit,
) {
  CompositionLocalProvider(LocalColorSchemeType provides schemeType) {
    ActualTheme(schemeType) {
      Surface(modifier = modifier) {
        content(schemeType)
      }
    }
  }
}

@Suppress("PreviewAnnotationNaming")
@Preview(
  name = "Screen",
  showBackground = true,
  uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED,
  widthDp = 3 * MY_PHONE_WIDTH_DP,
  heightDp = MY_PHONE_HEIGHT_DP,
)
annotation class ScreenPreview

private const val MY_PHONE_DPI = 400
const val MY_PHONE_WIDTH_DP = 1080 / (MY_PHONE_DPI / 160)
const val MY_PHONE_HEIGHT_DP = 2280 / (MY_PHONE_DPI / 160)

val SchemeTypes = persistentListOf(
  ColorSchemeType.Light,
  ColorSchemeType.Dark,
  ColorSchemeType.Midnight,
)
