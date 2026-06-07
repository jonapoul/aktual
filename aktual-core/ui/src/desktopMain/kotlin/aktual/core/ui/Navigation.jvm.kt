package aktual.core.ui

import aktual.core.model.AppCloser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// No-op on desktop. TODO: handle keyboard shortcuts?
@Composable actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

@Composable
actual fun rememberAppCloser(): AppCloser = remember { AppCloser { /* no-op. TODO: implement? */ } }
