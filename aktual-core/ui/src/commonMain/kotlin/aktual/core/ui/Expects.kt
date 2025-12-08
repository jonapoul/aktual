package aktual.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

@Composable
expect fun BackHandler(
  enabled: Boolean = true,
  onBack: () -> Unit,
)

@Composable
expect fun appCloser(): AppCloser

@Immutable
interface AppCloser {
  operator fun invoke()
}
