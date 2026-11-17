package aktual.core.ui

import androidx.compose.runtime.Composable

@Composable
actual fun rememberLocalNetworkPermissionRequest(
  openSettingsIfBlocked: Boolean,
  onResult: (Boolean) -> Unit,
): () -> Unit = { onResult(true) }
