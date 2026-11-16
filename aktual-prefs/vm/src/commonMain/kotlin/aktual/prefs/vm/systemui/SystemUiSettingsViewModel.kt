package aktual.prefs.vm.systemui

import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.SystemUiPreferences
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
class SystemUiSettingsViewModel
internal constructor(
  preferences: SystemUiPreferences,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  val state: StateFlow<SystemUiSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val showBottomBar by collectAsState(preferences.showBottomBar)
      val blurDialogs by collectAsState(preferences.hazeDialogs)
      val dialogBlurRadius by collectAsState(preferences.dialogBlurRadius)
      val dialogBlurAlpha by collectAsState(preferences.dialogBlurAlpha)
      SystemUiSettingsState(
        showStatusBar =
          BooleanPreference(
            value = showBottomBar,
            onChange = { launchAndSet(preferences.showBottomBar, it) },
          ),
        blurDialogs =
          BooleanPreference(
            value = blurDialogs,
            onChange = { launchAndSet(preferences.hazeDialogs, it) },
          ),
        dialogBlurRadius =
          BlurRadiusPreference(
            value = dialogBlurRadius,
            enabled = blurDialogs,
            onChange = { launchAndSet(preferences.dialogBlurRadius, it) },
          ),
        dialogBlurAlpha =
          BlurAlphaPreference(
            value = dialogBlurAlpha,
            enabled = blurDialogs,
            onChange = { launchAndSet(preferences.dialogBlurAlpha, it) },
          ),
      )
    }
}
