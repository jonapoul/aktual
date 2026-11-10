package aktual.budget.accounts.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

// Its own view models, cleared once it leaves the composition, so a sheet opened again starts
// afresh rather than picking up one that already saved
@Composable
fun rememberSheetViewModelStoreOwner(): ViewModelStoreOwner {
  val owner = remember { SheetViewModelStoreOwner() }
  DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }
  return owner
}

private class SheetViewModelStoreOwner : ViewModelStoreOwner {
  override val viewModelStore = ViewModelStore()
}
