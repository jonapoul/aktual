package dev.jonpoulton.actual.core.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf

@Composable
fun PreviewActualColumn(content: @Composable () -> Unit) {
  LazyColumn {
    items(SchemeTypes, key = { it }) { schemeType ->
      PreviewWithColorScheme(schemeType, content)
    }
  }
}

@Composable
fun PreviewActualRow(content: @Composable () -> Unit) {
  LazyRow {
    items(SchemeTypes, key = { it }) { schemeType ->
      PreviewWithColorScheme(schemeType, content)
    }
  }
}

@Composable
fun PreviewActualScreen(content: @Composable () -> Unit) {
  LazyRow {
    items(SchemeTypes, key = { it }) { schemeType ->
      Box(
        modifier = Modifier.width(MY_PHONE_WIDTH_DP.dp),
      ) {
        PreviewWithColorScheme(schemeType, content)
      }
    }
  }
}

@Composable
private fun PreviewWithColorScheme(schemeType: ActualColorSchemeType, content: @Composable () -> Unit) {
  ActualTheme(schemeType) {
    Surface {
      content()
    }
  }
}

@Preview(
  name = "Screen",
  showBackground = true,
  uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED,
  widthDp = 3 * MY_PHONE_WIDTH_DP,
  heightDp = MY_PHONE_HEIGHT_DP,
)
annotation class PreviewActualScreen

private const val MY_PHONE_DPI = 400
private const val MY_PHONE_WIDTH_DP = 1080 / (MY_PHONE_DPI / 160)
private const val MY_PHONE_HEIGHT_DP = 2280 / (MY_PHONE_DPI / 160)

private val SchemeTypes = persistentListOf(
  ActualColorSchemeType.Light,
  ActualColorSchemeType.Dark,
  ActualColorSchemeType.Midnight,
)
