package aktual.prefs.vm.root

import aktual.budget.model.Currency
import aktual.di.AppScope
import aktual.prefs.CurrencyPreferences
import aktual.prefs.FormatPreferences
import aktual.prefs.Preference
import aktual.prefs.SystemUiPreferences
import aktual.prefs.asStateFlow
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Stable
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
    val hazeAppBars by systemUiPreferences.hazeAppBars.collectAsStateFlow()
    val hazeDialogs by systemUiPreferences.hazeDialogs.collectAsStateFlow()
    val hazeRadius by systemUiPreferences.hazeRadius.collectAsStateFlow()
    val hazeAlpha by systemUiPreferences.hazeAlpha.collectAsStateFlow()
    val hidePreviewInAppSwitcher by
      systemUiPreferences.hidePreviewInAppSwitcher.collectAsStateFlow()
    val anyHazeEnabled = hazeAppBars || hazeDialogs
    return SystemUiConfigState(
      showStatusBar =
        BooleanPreference(
          value = showBottomBar,
          onChange = { systemUiPreferences.showBottomBar.launchAndSet(it) },
        ),
      hazeAppBars =
        BooleanPreference(
          value = hazeAppBars,
          onChange = { systemUiPreferences.hazeAppBars.launchAndSet(it) },
        ),
      hazeDialogs =
        BooleanPreference(
          value = hazeDialogs,
          onChange = { systemUiPreferences.hazeDialogs.launchAndSet(it) },
        ),
      hazeRadiusDp =
        HazeRadiusPreference(
          value = hazeRadius,
          enabled = anyHazeEnabled,
          onChange = { systemUiPreferences.hazeRadius.launchAndSet(it) },
        ),
      hazeAlpha =
        HazeAlphaPreference(
          value = hazeAlpha,
          enabled = anyHazeEnabled,
          onChange = { systemUiPreferences.hazeAlpha.launchAndSet(it) },
        ),
      hidePreviewInAppSwitcher =
        BooleanPreference(
          value = hidePreviewInAppSwitcher,
          visible = ShouldShowHidePreviewInAppSwitcher,
          onChange = { systemUiPreferences.hidePreviewInAppSwitcher.launchAndSet(it) },
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
