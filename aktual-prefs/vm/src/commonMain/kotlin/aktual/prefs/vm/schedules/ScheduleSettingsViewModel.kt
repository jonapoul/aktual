package aktual.prefs.vm.schedules

import aktual.di.AppScope
import aktual.prefs.SchedulePreferences
import aktual.prefs.asStateFlow
import aktual.prefs.vm.BooleanPreference
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class ScheduleSettingsViewModel(preferences: SchedulePreferences) : ViewModel() {
  private val showCompleted = preferences.showCompleted.asStateFlow(viewModelScope)

  val state: StateFlow<ScheduleSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val showCompleted by showCompleted.collectAsState()
      ScheduleSettingsState(
        showCompleted =
          BooleanPreference(
            value = showCompleted,
            onChange = { viewModelScope.launch { preferences.showCompleted.set(it) } },
          )
      )
    }
}
