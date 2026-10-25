package aktual.prefs.vm.schedules

import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.SchedulePreferences
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.asLoadedStateFlow
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
class ScheduleSettingsViewModel(
  preferences: SchedulePreferences,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val showCompleted = preferences.showCompleted.asLoadedStateFlow(viewModelScope)

  val state: StateFlow<ScheduleSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val showCompleted by showCompleted.collectAsState()
      ScheduleSettingsState(
        showCompleted =
          BooleanPreference(
            value = showCompleted,
            onChange = { viewModelScope.launch { preferences.showCompleted.set(it) } },
          ),
      )
    }
}
