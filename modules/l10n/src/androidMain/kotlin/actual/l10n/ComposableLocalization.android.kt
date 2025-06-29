package actual.l10n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun l10n(): Localization {
  val context = LocalContext.current
  return remember(context) { AndroidLocalization(context) }
}
