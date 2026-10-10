package aktual.prefs.vm.systemui

import aktual.di.AppScope
import aktual.prefs.SystemUiPreferences
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
class SystemUiSettingsViewModel(preferences: SystemUiPreferences) : ViewModel() {
  val state: StateFlow<SystemUiSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val showBottomBar by collectAsState(preferences.showBottomBar)
      val appBarEffect by collectAsState(preferences.appBarEffect)
      val hazeDialogs by collectAsState(preferences.hazeDialogs)
      val hazeRadius by collectAsState(preferences.hazeRadius)
      val hazeAlpha by collectAsState(preferences.hazeAlpha)
      val hidePreviewInAppSwitcher by collectAsState(preferences.hidePreviewInAppSwitcher)
      val anyHazeEnabled = appBarEffect != None || hazeDialogs
      SystemUiSettingsState(
        showStatusBar =
          BooleanPreference(
            value = showBottomBar,
            onChange = { launchAndSet(preferences.showBottomBar, it) },
          ),
        appBarEffect =
          ListPreference(
            value = appBarEffect,
            onChange = { launchAndSet(preferences.appBarEffect, it) },
          ),
        hazeDialogs =
          BooleanPreference(
            value = hazeDialogs,
            onChange = { launchAndSet(preferences.hazeDialogs, it) },
          ),
        hazeRadiusDp =
          HazeRadiusPreference(
            value = hazeRadius,
            enabled = anyHazeEnabled,
            onChange = { launchAndSet(preferences.hazeRadius, it) },
          ),
        hazeAlpha =
          HazeAlphaPreference(
            value = hazeAlpha,
            enabled = anyHazeEnabled,
            onChange = { launchAndSet(preferences.hazeAlpha, it) },
          ),
        hidePreviewInAppSwitcher =
          BooleanPreference(
            value = hidePreviewInAppSwitcher,
            visible = ShouldShowHidePreviewInAppSwitcher,
            onChange = { launchAndSet(preferences.hidePreviewInAppSwitcher, it) },
          ),
      )
    }
}
