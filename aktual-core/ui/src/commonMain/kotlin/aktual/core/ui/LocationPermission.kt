package aktual.core.ui

import androidx.compose.runtime.Composable

// Asks for location permission, then reports whether it was given. With openSettingsIfBlocked,
// opens the app's system settings instead when Android won't ask again
@Composable
expect fun rememberLocationPermissionRequest(
  openSettingsIfBlocked: Boolean = false,
  onResult: (Boolean) -> Unit,
): () -> Unit
