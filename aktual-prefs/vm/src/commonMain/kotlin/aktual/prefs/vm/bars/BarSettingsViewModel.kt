package aktual.prefs.vm.bars

import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.SystemUiPreferences
import aktual.prefs.vm.BlurAlphaPreference
import aktual.prefs.vm.BlurRadiusPreference
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
class BarSettingsViewModel
internal constructor(preferences: SystemUiPreferences, viewModelContext: ViewModelContext) :
  ViewModel(viewModelContext.newScope()) {
  val state: StateFlow<BarSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val barEffect by collectAsState(preferences.appBarEffect)
      val blurRadius by collectAsState(preferences.barBlurRadius)
      val blurAlpha by collectAsState(preferences.barBlurAlpha)
      val glassStyle by collectAsState(preferences.glassStyle)
      val glassCornerRadius by collectAsState(preferences.glassCornerRadius)
      BarSettingsState(
        barEffect =
          ListPreference(
            value = barEffect,
            onChange = { launchAndSet(preferences.appBarEffect, it) },
          ),
        blurRadius =
          BlurRadiusPreference(
            value = blurRadius,
            enabled = barEffect == Blur,
            onChange = { launchAndSet(preferences.barBlurRadius, it) },
          ),
        blurAlpha =
          BlurAlphaPreference(
            value = blurAlpha,
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
      )
    }
}
