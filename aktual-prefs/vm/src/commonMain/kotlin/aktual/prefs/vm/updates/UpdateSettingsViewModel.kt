package aktual.prefs.vm.updates

import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.AppPreferences
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.collectAsState
import aktual.prefs.vm.launchAndSet
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class UpdateSettingsViewModel
internal constructor(preferences: AppPreferences, viewModelContext: ViewModelContext) :
  ViewModel(viewModelContext.newScope()) {
  val state: StateFlow<UpdateSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val checkNightlies by collectAsState(preferences.checkForNightlyUpdates)
      UpdateSettingsState(
        checkNightlies =
          BooleanPreference(
            value = checkNightlies,
            onChange = { launchAndSet(preferences.checkForNightlyUpdates, it) },
          ),
      )
    }
}
