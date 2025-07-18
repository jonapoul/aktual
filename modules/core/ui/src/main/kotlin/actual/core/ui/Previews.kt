@file:Suppress("ModifierNotUsedAtRoot")

package actual.core.ui

import actual.budget.model.NumberFormat
import actual.budget.model.NumberFormatConfig
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
fun WithCompositionLocals(
  isPrivacyEnabled: Boolean = false,
  format: NumberFormat = NumberFormat.Default,
  hideFractions: Boolean = false,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalNumberFormatConfig provides NumberFormatConfig(format, hideFractions),
    LocalPrivacyEnabled provides isPrivacyEnabled,
  ) {
    content()
  }
}

@Composable
fun PreviewColumn(
  modifier: Modifier = Modifier,
  isPrivacyEnabled: Boolean = false,
  content: @Composable (ColorSchemeType) -> Unit,
) {
  LazyColumn {
    items(SchemeTypes, key = { it.ordinal }) { schemeType ->
      Box(modifier = modifier) {
        PreviewWithColorScheme(
          schemeType = schemeType,
          isPrivacyEnabled = isPrivacyEnabled,
          content = content,
        )
      }
    }
  }
}

@Composable
fun PreviewActualRow(
  modifier: Modifier = Modifier,
  privacyFilter: Boolean = false,
  content: @Composable (ColorSchemeType) -> Unit,
) {
  LazyRow {
    items(SchemeTypes, key = { it.ordinal }) { schemeType ->
      Box(modifier = modifier) {
        PreviewWithColorScheme(
          schemeType = schemeType,
          isPrivacyEnabled = privacyFilter,
          content = content,
        )
      }
    }
  }
}

@Composable
fun PreviewScreen(
  modifier: Modifier = Modifier,
  privacyFilter: Boolean = false,
  content: @Composable (ColorSchemeType) -> Unit,
) {
  LazyRow {
    items(SchemeTypes, key = { it.ordinal }) { schemeType ->
      PreviewWithColorScheme(
        modifier = modifier
          .width(MY_PHONE_WIDTH_DP.dp)
          .height(MY_PHONE_HEIGHT_DP.dp),
        schemeType = schemeType,
        isPrivacyEnabled = privacyFilter,
        content = { content(schemeType) },
      )
    }
  }
}

@Composable
fun PreviewWithColorScheme(
  schemeType: ColorSchemeType,
  modifier: Modifier = Modifier,
  isPrivacyEnabled: Boolean = false,
  content: @Composable (ColorSchemeType) -> Unit,
) = WithCompositionLocals(
  isPrivacyEnabled = isPrivacyEnabled,
) {
  ActualTheme(schemeType) {
    Surface(modifier = modifier) {
      content(schemeType)
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
