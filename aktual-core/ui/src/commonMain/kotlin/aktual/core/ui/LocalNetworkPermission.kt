package aktual.core.ui

import androidx.compose.runtime.Composable

// Asks for Android 17's local network permission (shown as "Nearby devices"), then reports whether
// it was given. Reports true straight away where there's nothing to ask for. With
// openSettingsIfBlocked, opens the app's system settings instead when Android won't ask again, and
// reports once the user comes back from them
@Composable
expect fun rememberLocalNetworkPermissionRequest(
  openSettingsIfBlocked: Boolean = false,
  onResult: (Boolean) -> Unit,
): () -> Unit
