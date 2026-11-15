package aktual.prefs.vm.appicon

import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.AppIcon
import aktual.prefs.AppIconController
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class AppIconSettingsViewModel
internal constructor(private val controller: AppIconController, viewModelContext: ViewModelContext) :
  ViewModel(viewModelContext.newScope()) {
  val selected: StateFlow<AppIcon> = controller.current

  fun select(icon: AppIcon) {
    viewModelScope.launch { controller.set(icon) }
  }
}
