package aktual.prefs.vm.currency

import aktual.di.AppScope
import aktual.prefs.CurrencyPreferences
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
class CurrencySettingsViewModel(preferences: CurrencyPreferences) : ViewModel() {
  val state: StateFlow<CurrencySettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val currency by collectAsState(preferences.currency)
      val symbolPosition by collectAsState(preferences.symbolPosition)
      val spaceBetweenAmountAndSymbol by collectAsState(preferences.spaceBetweenAmountAndSymbol)
      CurrencySettingsState(
        currency =
          ListPreference(
            value = currency,
            onChange = { launchAndSet(preferences.currency, it) },
          ),
        symbolPosition =
          ListPreference(
            value = symbolPosition,
            enabled = currency != None,
            onChange = { launchAndSet(preferences.symbolPosition, it) },
          ),
        spaceBetweenAmountAndSymbol =
          BooleanPreference(
            value = spaceBetweenAmountAndSymbol,
            enabled = currency != None,
            onChange = { launchAndSet(preferences.spaceBetweenAmountAndSymbol, it) },
          ),
      )
    }
}
