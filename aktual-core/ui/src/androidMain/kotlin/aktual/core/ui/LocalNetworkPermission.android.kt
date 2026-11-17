package aktual.core.ui

import android.Manifest.permission.ACCESS_LOCAL_NETWORK
import android.os.Build
import androidx.compose.runtime.Composable

@Composable
actual fun rememberLocalNetworkPermissionRequest(
  openSettingsIfBlocked: Boolean,
  onResult: (Boolean) -> Unit,
): () -> Unit {
  val request =
    rememberPermissionRequest(
      permissions = arrayOf(ACCESS_LOCAL_NETWORK),
      openSettingsIfBlocked = openSettingsIfBlocked,
      onResult = onResult,
    )
  // Older versions don't have the permission, and don't restrict the local network
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
    request
  } else {
    { onResult(true) }
  }
}
