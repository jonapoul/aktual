package aktual.prefs.vm.format

import aktual.di.AppScope
import aktual.prefs.FormatPreferences
import aktual.prefs.vm.BooleanPreference
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
class FormatSettingsViewModel(preferences: FormatPreferences) : ViewModel() {
  val state: StateFlow<FormatSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val numberFormat by collectAsState(preferences.numberFormat)
      val dateFormat by collectAsState(preferences.dateFormat)
      val firstDayOfWeek by collectAsState(preferences.firstDayOfWeek)
      val hideFraction by collectAsState(preferences.hideFraction)
      FormatSettingsState(
        numberFormat =
          ListPreference(
            value = numberFormat,
            onChange = { launchAndSet(preferences.numberFormat, it) },
          ),
        dateFormat =
          ListPreference(
            value = dateFormat,
            onChange = { launchAndSet(preferences.dateFormat, it) },
          ),
        firstDayOfWeek =
          ListPreference(
            value = firstDayOfWeek,
            onChange = { launchAndSet(preferences.firstDayOfWeek, it) },
          ),
        hideFraction =
          BooleanPreference(
            value = hideFraction,
            onChange = { launchAndSet(preferences.hideFraction, it) },
          ),
      )
    }
}
