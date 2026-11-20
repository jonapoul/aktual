package aktual.core.ui

import androidx.compose.runtime.Composable
import kotlin.contracts.contract

@Composable
fun <T : Any> LoadingScreenIfNull(value: T?, content: @Composable (T) -> Unit) {
  contract { returns() implies (value != null) }
  if (value == null) LoadingScreen() else content(value)
}
