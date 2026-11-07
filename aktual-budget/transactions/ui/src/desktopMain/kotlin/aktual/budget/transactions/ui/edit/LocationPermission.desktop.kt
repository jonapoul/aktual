package aktual.budget.transactions.ui.edit

import androidx.compose.runtime.Composable

@Composable
internal actual fun rememberLocationPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit = {
  onResult(false)
}
