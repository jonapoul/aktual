package aktual.prefs.vm.systemui

import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
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
class SystemUiSettingsViewModel
internal constructor(
  preferences: SystemUiPreferences,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  val state: StateFlow<SystemUiSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val showBottomBar by collectAsState(preferences.showBottomBar)
      val barEffect by collectAsState(preferences.appBarEffect)
      val barBlurRadius by collectAsState(preferences.barBlurRadius)
      val barBlurAlpha by collectAsState(preferences.barBlurAlpha)
      val glassStyle by collectAsState(preferences.glassStyle)
      val glassCornerRadius by collectAsState(preferences.glassCornerRadius)
      val blurDialogs by collectAsState(preferences.hazeDialogs)
      val dialogBlurRadius by collectAsState(preferences.dialogBlurRadius)
      val dialogBlurAlpha by collectAsState(preferences.dialogBlurAlpha)
      SystemUiSettingsState(
        showStatusBar =
          BooleanPreference(
            value = showBottomBar,
            onChange = { launchAndSet(preferences.showBottomBar, it) },
          ),
        barEffect =
          ListPreference(
            value = barEffect,
            onChange = { launchAndSet(preferences.appBarEffect, it) },
          ),
        barBlurRadius =
          BlurRadiusPreference(
            value = barBlurRadius,
            enabled = barEffect == Blur,
            onChange = { launchAndSet(preferences.barBlurRadius, it) },
          ),
        barBlurAlpha =
          BlurAlphaPreference(
            value = barBlurAlpha,
            enabled = barEffect == Blur,
            onChange = { launchAndSet(preferences.barBlurAlpha, it) },
          ),
        glassStyle =
          ListPreference(
            value = glassStyle,
            enabled = barEffect == Glass,
            onChange = { launchAndSet(preferences.glassStyle, it) },
          ),
        glassCornerRadiusDp =
          GlassCornerRadiusPreference(
            value = glassCornerRadius,
            enabled = barEffect == Glass,
            onChange = { launchAndSet(preferences.glassCornerRadius, it) },
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
