package aktual.core.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.edit
import androidx.core.net.toUri

// Granted when any of the permissions is given. When it opens the app's settings, it reports the
// result once the user leaves them, so callers can carry on with what they've chosen
@Composable
internal fun rememberPermissionRequest(
  permissions: Array<String>,
  openSettingsIfBlocked: Boolean,
  onResult: (Boolean) -> Unit,
): () -> Unit {
  val activity = LocalActivity.current

  // Reports when the user leaves settings, wherever it opened, e.g. beside the app in split screen
  val settingsLauncher =
    rememberLauncherForActivityResult(StartActivityForResult()) {
      onResult(activity?.isAnyGranted(permissions) == true)
    }

  var hadRationaleBefore by rememberSaveable { mutableStateOf(false) }
  val launcher =
    rememberLauncherForActivityResult(RequestMultiplePermissions()) { results ->
      val isGranted = results.values.any { it }
      val isBlocked =
        activity != null && isBlocked(activity, permissions.first(), isGranted, hadRationaleBefore)
      if (!isGranted && openSettingsIfBlocked && isBlocked) {
        val uri = "package:${activity.packageName}".toUri()
        settingsLauncher.launch(Intent(ACTION_APPLICATION_DETAILS_SETTINGS, uri))
      } else {
        onResult(isGranted)
      }
    }
  return {
    hadRationaleBefore =
      activity != null && shouldShowRequestPermissionRationale(activity, permissions.first())
    launcher.launch(permissions)
  }
}

// Android shows a rationale after the first denial, and stops after the second, when it stops
// asking. Dismissing the dialog without answering shows no rationale either, so a first dismissal
// looks the same as being blocked unless we remember an earlier denial. A rationale before this
// request means the dialog was just shown and denied, so the user has already answered
private fun isBlocked(
  activity: Activity,
  permission: String,
  isGranted: Boolean,
  hadRationaleBefore: Boolean,
): Boolean {
  val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  val hasRationale = shouldShowRequestPermissionRationale(activity, permission)
  if (isGranted) {
    prefs.edit { remove(permission) }
    return false
  }
  if (hasRationale || hadRationaleBefore) {
    prefs.edit { putBoolean(permission, true) }
  }
  return !hasRationale && !hadRationaleBefore && prefs.getBoolean(permission, false)
}

private const val PREFS_NAME = "aktual_permission_denials"

private fun Context.isAnyGranted(permissions: Array<String>): Boolean = permissions.any {
  checkSelfPermission(this, it) == PERMISSION_GRANTED
}
