package aktual.app.desktop

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

internal class JvmViewModelStoreOwner : ViewModelStoreOwner {
  override val viewModelStore = ViewModelStore()
}
