package aktual.settings.vm.root

import aktual.core.prefs.AppGlobalPreferences
import aktual.settings.vm.BooleanPreference
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.jonpoulton.preferences.core.asStateFlow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow

@Inject
@ViewModelKey(SettingsViewModel::class)
@ContributesIntoMap(AppScope::class)
class SettingsViewModel internal constructor(preferences: AppGlobalPreferences) : ViewModel() {
  private val showBottomBarPref = preferences.showBottomBar
  private val showBottomBar = showBottomBarPref.asStateFlow(viewModelScope)

  val state: StateFlow<SettingsScreenState> =
    viewModelScope.launchMolecule(Immediate) {
      val showBottomBar by showBottomBar.collectAsState()
      SettingsScreenState(
        showBottomBar =
          BooleanPreference(
            value = showBottomBar,
            onValueChange = showBottomBarPref::set,
            enabled = true,
          )
      )
    }
}
