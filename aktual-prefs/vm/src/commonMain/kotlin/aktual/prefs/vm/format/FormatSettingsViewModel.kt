package aktual.prefs.vm.format

import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.FormatPreferences
import aktual.prefs.vm.ListPreference
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
class FormatSettingsViewModel
internal constructor(preferences: FormatPreferences, viewModelContext: ViewModelContext) :
  ViewModel(viewModelContext.newScope()) {
  val state: StateFlow<FormatSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val distanceUnit by collectAsState(preferences.distanceUnit)
      FormatSettingsState(
        distanceUnit =
          ListPreference(
            value = distanceUnit,
            onChange = { launchAndSet(preferences.distanceUnit, it) },
          ),
      )
    }
}
