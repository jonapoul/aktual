package aktual.settings.vm.root

import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormat
import aktual.core.prefs.AppGlobalPreferences
import aktual.core.prefs.Preference
import aktual.core.prefs.asStateFlow
import aktual.settings.vm.BooleanPreference
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
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Inject
@ViewModelKey(SettingsViewModel::class)
@ContributesIntoMap(AppScope::class)
class SettingsViewModel internal constructor(preferences: AppGlobalPreferences) : ViewModel() {
  private val showBottomBarPref = preferences.showBottomBar
  private val numberFormatPref = preferences.numberFormat
  private val hideFractionPref = preferences.hideFraction
  private val dateFormatPref = preferences.dateFormat
  private val firstDayOfWeekPref = preferences.firstDayOfWeek
  private val currencyPref = preferences.currency
  private val currencySymbolPositionPref = preferences.currencySymbolPosition
  private val currencySpaceBetweenAmountAndSymbolPref =
    preferences.currencySpaceBetweenAmountAndSymbol

  val state: StateFlow<SettingsScreenState> =
    viewModelScope.launchMolecule(Immediate) {
      val showBottomBar by showBottomBarPref.collectAsStateFlow()
      val numberFormat by numberFormatPref.collectAsStateFlow()
      val hideFraction by hideFractionPref.collectAsStateFlow()
      val dateFormat by dateFormatPref.collectAsStateFlow()
      val firstDayOfWeek by firstDayOfWeekPref.collectAsStateFlow()
      val currency by currencyPref.collectAsStateFlow()
      val currencySymbolPosition by currencySymbolPositionPref.collectAsStateFlow()
      val currencySpaceBetweenAmountAndSymbol by
        currencySpaceBetweenAmountAndSymbolPref.collectAsStateFlow()

      SettingsScreenState(
        showBottomBar = BooleanPreference(value = showBottomBar),
        numberFormat = NumberFormatPreference(selected = numberFormat),
        hideFraction = BooleanPreference(value = hideFraction),
        dateFormat = dateFormat,
        firstDayOfWeek = firstDayOfWeek,
        currency = CurrencyPreference(currency),
        currencySymbolPosition =
          CurrencySymbolPositionPreference(currencySymbolPosition, enabled = currency != None),
        currencySpaceBetweenAmountAndSymbol =
          BooleanPreference(currencySpaceBetweenAmountAndSymbol, enabled = currency != None),
      )
    }

  fun showBottomBar(value: Boolean) {
    viewModelScope.launch { showBottomBarPref.set(value) }
  }

  fun numberFormat(value: NumberFormat) {
    viewModelScope.launch { numberFormatPref.set(value) }
  }

  fun hideFraction(value: Boolean) {
    viewModelScope.launch { hideFractionPref.set(value) }
  }

  fun dateFormat(value: DateFormat) {
    viewModelScope.launch { dateFormatPref.set(value) }
  }

  fun firstDayOfWeek(value: FirstDayOfWeek) {
    viewModelScope.launch { firstDayOfWeekPref.set(value) }
  }

  fun currency(value: Currency) {
    viewModelScope.launch { currencyPref.set(value) }
  }

  fun currencySymbolPosition(value: CurrencySymbolPosition) {
    viewModelScope.launch { currencySymbolPositionPref.set(value) }
  }

  fun currencySpaceBetweenAmountAndSymbol(value: Boolean) {
    viewModelScope.launch { currencySpaceBetweenAmountAndSymbolPref.set(value) }
  }

  @Composable
  private fun <T : Any> Preference<T>.collectAsStateFlow(): State<T> {
    val stateFlow = remember(this) { asStateFlow(viewModelScope) }
    return stateFlow.collectAsState()
  }
}
