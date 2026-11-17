package aktual.core.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.compose.runtime.Composable

// Coarse alone is enough, if the user picks an approximate location
@Composable
actual fun rememberLocationPermissionRequest(
  openSettingsIfBlocked: Boolean,
  onResult: (Boolean) -> Unit,
): () -> Unit =
  rememberPermissionRequest(
    permissions = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),
    openSettingsIfBlocked = openSettingsIfBlocked,
    onResult = onResult,
  )
