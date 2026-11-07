package aktual.core.ui

import aktual.core.utils.AppCloser
import androidx.compose.runtime.Composable

@Composable expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)

@Composable expect fun rememberAppCloser(): AppCloser
