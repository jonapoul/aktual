package aktual.settings.vm.root

import aktual.budget.model.Currency
import aktual.core.prefs.AppGlobalPreferences
import aktual.core.prefs.Preference
import aktual.core.prefs.asStateFlow
import aktual.settings.vm.BooleanPreference
import aktual.settings.vm.ListPreference
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

@ViewModelKey(SettingsViewModel::class)
@ContributesIntoMap(AppScope::class)
class SettingsViewModel internal constructor(private val preferences: AppGlobalPreferences) :
  ViewModel() {
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
    val showBottomBar by preferences.showBottomBar.collectAsStateFlow()
    val blurTopBar by preferences.blurTopBar.collectAsStateFlow()
    val blurStatusBar by preferences.blurStatusBar.collectAsStateFlow()
    val blurDialogs by preferences.blurDialogs.collectAsStateFlow()
    val blurRadius by preferences.blurRadius.collectAsStateFlow()
    val blurAlpha by preferences.blurAlpha.collectAsStateFlow()
    val anyBlurEnabled = blurStatusBar || blurTopBar || blurDialogs
    return SystemUiConfigState(
      showStatusBar =
        BooleanPreference(
          value = showBottomBar,
          onChange = { preferences.showBottomBar.launchAndSet(it) },
        ),
      blurTopBar =
        BooleanPreference(
          value = blurTopBar,
          onChange = { preferences.blurTopBar.launchAndSet(it) },
        ),
      blurStatusBar =
        BooleanPreference(
          value = blurStatusBar,
          onChange = { preferences.blurStatusBar.launchAndSet(it) },
        ),
      blurDialogs =
        BooleanPreference(
          value = blurDialogs,
          onChange = { preferences.blurDialogs.launchAndSet(it) },
        ),
      blurRadiusDp =
        BlurRadiusPreference(
          value = blurRadius,
          enabled = anyBlurEnabled,
          onChange = { preferences.blurRadius.launchAndSet(it) },
        ),
      blurAlpha =
        BlurAlphaPreference(
          value = blurAlpha,
          enabled = anyBlurEnabled,
          onChange = { preferences.blurAlpha.launchAndSet(it) },
        ),
    )
  }

  @Composable
  private fun currencyState(): CurrencyConfigState {
    val currency by preferences.currency.collectAsStateFlow()
    val symbolPosition by preferences.currencySymbolPosition.collectAsStateFlow()
    val spaceBetweenAmountAndSymbol by
      preferences.currencySpaceBetweenAmountAndSymbol.collectAsStateFlow()
    return CurrencyConfigState(
      currency =
        ListPreference(value = currency, onChange = { preferences.currency.launchAndSet(it) }),
      symbolPosition =
        ListPreference(
          value = symbolPosition,
          enabled = currency != Currency.None,
          onChange = { preferences.currencySymbolPosition.launchAndSet(it) },
        ),
      spaceBetweenAmountAndSymbol =
        BooleanPreference(
          value = spaceBetweenAmountAndSymbol,
          enabled = currency != Currency.None,
          onChange = { preferences.currencySpaceBetweenAmountAndSymbol.launchAndSet(it) },
        ),
    )
  }

  @Composable
  private fun formatState(): FormatConfigState {
    val numberFormat by preferences.numberFormat.collectAsStateFlow()
    val dateFormat by preferences.dateFormat.collectAsStateFlow()
    val firstDayOfWeek by preferences.firstDayOfWeek.collectAsStateFlow()
    val hideFraction by preferences.hideFraction.collectAsStateFlow()
    return FormatConfigState(
      numberFormat =
        ListPreference(
          value = numberFormat,
          onChange = { preferences.numberFormat.launchAndSet(it) },
        ),
      dateFormat =
        ListPreference(value = dateFormat, onChange = { preferences.dateFormat.launchAndSet(it) }),
      firstDayOfWeek =
        ListPreference(
          value = firstDayOfWeek,
          onChange = { preferences.firstDayOfWeek.launchAndSet(it) },
        ),
      hideFraction =
        BooleanPreference(
          value = hideFraction,
          onChange = { preferences.hideFraction.launchAndSet(it) },
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
