package aktual.nav.core

import aktual.core.model.AppCloser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember

// No-op on desktop. TODO: handle keyboard shortcuts?
@Composable actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

@Composable actual fun rememberAppCloser(): AppCloser = remember { JvmAppCloser() }

@Immutable
private class JvmAppCloser : AppCloser {
  override operator fun invoke() {
    // no-op. TODO: implement
  }
}
