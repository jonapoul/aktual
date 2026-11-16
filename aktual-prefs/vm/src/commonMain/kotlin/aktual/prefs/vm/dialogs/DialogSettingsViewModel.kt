package aktual.prefs.vm.dialogs

import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.SystemUiPreferences
import aktual.prefs.vm.BlurAlphaPreference
import aktual.prefs.vm.BlurRadiusPreference
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
class DialogSettingsViewModel
internal constructor(preferences: SystemUiPreferences, viewModelContext: ViewModelContext) :
  ViewModel(viewModelContext.newScope()) {
  val state: StateFlow<DialogSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val blurDialogs by collectAsState(preferences.hazeDialogs)
      val blurRadius by collectAsState(preferences.dialogBlurRadius)
      val blurAlpha by collectAsState(preferences.dialogBlurAlpha)
      DialogSettingsState(
        blurDialogs =
          BooleanPreference(
            value = blurDialogs,
            onChange = { launchAndSet(preferences.hazeDialogs, it) },
          ),
        blurRadius =
          BlurRadiusPreference(
            value = blurRadius,
            enabled = blurDialogs,
            onChange = { launchAndSet(preferences.dialogBlurRadius, it) },
          ),
        blurAlpha =
          BlurAlphaPreference(
            value = blurAlpha,
            enabled = blurDialogs,
            onChange = { launchAndSet(preferences.dialogBlurAlpha, it) },
          ),
      )
    }
}
