package aktual.budget.transactions.ui.edit

import androidx.compose.runtime.Composable

// Asks for location permission, then reports whether it was given
@Composable
internal expect fun rememberLocationPermissionRequest(onResult: (Boolean) -> Unit): () -> Unit
