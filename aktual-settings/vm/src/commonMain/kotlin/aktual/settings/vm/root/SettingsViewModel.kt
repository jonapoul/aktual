package aktual.settings.vm.root

import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.NumberFormat
import aktual.core.prefs.AppGlobalPreferences
import aktual.settings.vm.BooleanPreference
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.jonpoulton.preferences.core.asStateFlow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow

@Inject
@ViewModelKey(SettingsViewModel::class)
@ContributesIntoMap(AppScope::class)
class SettingsViewModel internal constructor(preferences: AppGlobalPreferences) : ViewModel() {
  private val showBottomBarPref = preferences.showBottomBar
  private val showBottomBar = showBottomBarPref.asStateFlow(viewModelScope)

  private val numberFormatPref = preferences.numberFormat
  private val numberFormat = numberFormatPref.asStateFlow(viewModelScope)

  private val hideFractionPref = preferences.hideFraction
  private val hideFraction = hideFractionPref.asStateFlow(viewModelScope)

  private val currencyPref = preferences.currency
  private val currency = currencyPref.asStateFlow(viewModelScope)

  private val currencySymbolPositionPref = preferences.currencySymbolPosition
  private val currencySymbolPosition = currencySymbolPositionPref.asStateFlow(viewModelScope)

  private val currencySpaceBetweenAmountAndSymbolPref =
    preferences.currencySpaceBetweenAmountAndSymbol
  private val currencySpaceBetweenAmountAndSymbol =
    currencySpaceBetweenAmountAndSymbolPref.asStateFlow(viewModelScope)

  val state: StateFlow<SettingsScreenState> =
    viewModelScope.launchMolecule(Immediate) {
      val showBottomBar by showBottomBar.collectAsState()
      val numberFormat by numberFormat.collectAsState()
      val hideFraction by hideFraction.collectAsState()
      val currency by currency.collectAsState()
      val currencySymbolPosition by currencySymbolPosition.collectAsState()
      val currencySpaceBetweenAmountAndSymbol by
        currencySpaceBetweenAmountAndSymbol.collectAsState()

      SettingsScreenState(
        showBottomBar = BooleanPreference(value = showBottomBar),
        numberFormat = NumberFormatPreference(selected = numberFormat),
        hideFraction = BooleanPreference(value = hideFraction),
        currency = CurrencyPreference(currency),
        currencySymbolPosition =
          CurrencySymbolPositionPreference(currencySymbolPosition, enabled = currency != None),
        currencySpaceBetweenAmountAndSymbol =
          BooleanPreference(currencySpaceBetweenAmountAndSymbol, enabled = currency != None),
      )
    }

  fun showBottomBar(value: Boolean) = showBottomBarPref.set(value)

  fun numberFormat(value: NumberFormat) = numberFormatPref.set(value)

  fun hideFraction(value: Boolean) = hideFractionPref.set(value)

  fun currency(value: Currency) = currencyPref.set(value)

  fun currencySymbolPosition(value: CurrencySymbolPosition) = currencySymbolPositionPref.set(value)

  fun currencySpaceBetweenAmountAndSymbol(value: Boolean) =
    currencySpaceBetweenAmountAndSymbolPref.set(value)
}
