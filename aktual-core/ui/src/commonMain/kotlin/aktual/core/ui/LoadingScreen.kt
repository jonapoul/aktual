package aktual.core.ui

import aktual.core.theme.Theme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlin.contracts.contract

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) { AnimatedLoading() }
}

@Composable
fun <T : Any> LoadingScreenIfNotNull(value: T?, content: @Composable (T) -> Unit) {
  contract { returns() implies (value != null) }
  if (value == null) LoadingScreen() else content(value)
}

@Preview
@Composable
private fun PreviewLoadingScreen(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) { LoadingScreen() }
