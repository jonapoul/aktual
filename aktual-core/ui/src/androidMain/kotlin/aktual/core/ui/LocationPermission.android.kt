package aktual.core.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.net.toUri

// Coarse alone is enough, if the user picks an approximate location
@Composable
actual fun rememberLocationPermissionRequest(
  openSettingsIfBlocked: Boolean,
  onResult: (Boolean) -> Unit,
): () -> Unit {
  val activity = LocalActivity.current
  val launcher =
    rememberLauncherForActivityResult(RequestMultiplePermissions()) { results ->
      val isGranted = results.values.any { it }
      // Without a rationale to show after a denial, Android has stopped asking
      val isBlocked =
        activity != null && !shouldShowRequestPermissionRationale(activity, ACCESS_FINE_LOCATION)
      if (!isGranted && openSettingsIfBlocked && isBlocked) {
        val uri = "package:${activity.packageName}".toUri()
        activity.startActivity(Intent(ACTION_APPLICATION_DETAILS_SETTINGS, uri))
      }
      onResult(isGranted)
    }
  return { launcher.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) }
}
