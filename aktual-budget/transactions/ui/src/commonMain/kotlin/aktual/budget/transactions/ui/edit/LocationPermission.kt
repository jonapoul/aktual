package aktual.budget.transactions.ui.edit

import aktual.core.ui.rememberLocationPermissionRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// Runs an action that needs the user's location, asking for permission first if it's needed
@Composable
internal fun rememberWithLocation(
  needsPermission: () -> Boolean,
  onDeny: () -> Unit,
): (() -> Unit) -> Unit {
  var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
  val requestPermission = rememberLocationPermissionRequest { isGranted ->
    val action = pending
    pending = null
    if (isGranted) action?.invoke() else onDeny()
  }
  return { action ->
    if (needsPermission()) {
      pending = action
      requestPermission()
    } else {
      action()
    }
  }
}
