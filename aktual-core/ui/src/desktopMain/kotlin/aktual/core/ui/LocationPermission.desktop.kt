package aktual.core.ui

import androidx.compose.runtime.Composable

@Composable
actual fun rememberLocationPermissionRequest(
  openSettingsIfBlocked: Boolean,
  onResult: (Boolean) -> Unit,
): () -> Unit = { onResult(false) }
