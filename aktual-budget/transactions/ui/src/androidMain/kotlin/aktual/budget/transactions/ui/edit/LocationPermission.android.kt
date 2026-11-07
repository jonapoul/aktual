package aktual.budget.transactions.ui.edit

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.runtime.Composable

// Coarse alone is enough, if the user picks an approximate location
@Composable
internal actual fun rememberLocationPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit {
  val launcher =
    rememberLauncherForActivityResult(RequestMultiplePermissions()) { results ->
      onResult(results.values.any { it })
    }
  return { launcher.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) }
}
