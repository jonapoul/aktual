package aktual.prefs.vm.root

import aktual.budget.model.Currency
import aktual.prefs.CurrencyPreferences
import aktual.prefs.FormatPreferences
import aktual.prefs.Preference
import aktual.prefs.SystemUiPreferences
import aktual.prefs.asStateFlow
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@ViewModelKey
@ContributesIntoMap(AppScope::class)
class SettingsViewModel(
  private val systemUiPreferences: SystemUiPreferences,
  private val formatPreferences: FormatPreferences,
  private val currencyPreferences: CurrencyPreferences,
) : ViewModel() {
  val state: StateFlow<SettingsScreenState> =
    viewModelScope.launchMolecule(Immediate) {
      SettingsScreenState(
        systemUi = systemUiState(),
        format = formatState(),
        currency = currencyState(),
      )
    }

  @Composable
  private fun systemUiState(): SystemUiConfigState {
    val showBottomBar by systemUiPreferences.showBottomBar.collectAsStateFlow()
    val blurAppBars by systemUiPreferences.blurAppBars.collectAsStateFlow()
    val blurDialogs by systemUiPreferences.blurDialogs.collectAsStateFlow()
    val blurRadius by systemUiPreferences.blurRadius.collectAsStateFlow()
    val blurAlpha by systemUiPreferences.blurAlpha.collectAsStateFlow()
    val anyBlurEnabled = blurAppBars || blurDialogs
    return SystemUiConfigState(
      showStatusBar =
        BooleanPreference(
          value = showBottomBar,
          onChange = { systemUiPreferences.showBottomBar.launchAndSet(it) },
        ),
      blurAppBars =
        BooleanPreference(
          value = blurAppBars,
          onChange = { systemUiPreferences.blurAppBars.launchAndSet(it) },
        ),
      blurDialogs =
        BooleanPreference(
          value = blurDialogs,
          onChange = { systemUiPreferences.blurDialogs.launchAndSet(it) },
        ),
      blurRadiusDp =
        BlurRadiusPreference(
          value = blurRadius,
          enabled = anyBlurEnabled,
          onChange = { systemUiPreferences.blurRadius.launchAndSet(it) },
        ),
      blurAlpha =
        BlurAlphaPreference(
          value = blurAlpha,
          enabled = anyBlurEnabled,
          onChange = { systemUiPreferences.blurAlpha.launchAndSet(it) },
        ),
    )
  }

  @Composable
  private fun currencyState(): CurrencyConfigState {
    val currency by currencyPreferences.currency.collectAsStateFlow()
    val symbolPosition by currencyPreferences.symbolPosition.collectAsStateFlow()
    val spaceBetweenAmountAndSymbol by
      currencyPreferences.spaceBetweenAmountAndSymbol.collectAsStateFlow()
    return CurrencyConfigState(
      currency =
        ListPreference(
          value = currency,
          onChange = { currencyPreferences.currency.launchAndSet(it) },
        ),
      symbolPosition =
        ListPreference(
          value = symbolPosition,
          enabled = currency != Currency.None,
          onChange = { currencyPreferences.symbolPosition.launchAndSet(it) },
        ),
      spaceBetweenAmountAndSymbol =
        BooleanPreference(
          value = spaceBetweenAmountAndSymbol,
          enabled = currency != Currency.None,
          onChange = { currencyPreferences.spaceBetweenAmountAndSymbol.launchAndSet(it) },
        ),
    )
  }

  @Composable
  private fun formatState(): FormatConfigState {
    val numberFormat by formatPreferences.numberFormat.collectAsStateFlow()
    val dateFormat by formatPreferences.dateFormat.collectAsStateFlow()
    val firstDayOfWeek by formatPreferences.firstDayOfWeek.collectAsStateFlow()
    val hideFraction by formatPreferences.hideFraction.collectAsStateFlow()
    return FormatConfigState(
      numberFormat =
        ListPreference(
          value = numberFormat,
          onChange = { formatPreferences.numberFormat.launchAndSet(it) },
        ),
      dateFormat =
        ListPreference(
          value = dateFormat,
          onChange = { formatPreferences.dateFormat.launchAndSet(it) },
        ),
      firstDayOfWeek =
        ListPreference(
          value = firstDayOfWeek,
          onChange = { formatPreferences.firstDayOfWeek.launchAndSet(it) },
        ),
      hideFraction =
        BooleanPreference(
          value = hideFraction,
          onChange = { formatPreferences.hideFraction.launchAndSet(it) },
        ),
    )
  }

  @Composable
  private fun <T : Any> Preference<T>.collectAsStateFlow(): State<T> {
    val stateFlow = remember(this) { asStateFlow(viewModelScope) }
    return stateFlow.collectAsState()
  }

  private fun <T : Any> Preference<T>.launchAndSet(value: T) {
    viewModelScope.launch { set(value) }
  }
}
