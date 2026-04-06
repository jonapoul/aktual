package aktual.nav.core

import aktual.core.model.AppCloser
import androidx.compose.runtime.Composable

@Composable expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)

@Composable expect fun rememberAppCloser(): AppCloser
