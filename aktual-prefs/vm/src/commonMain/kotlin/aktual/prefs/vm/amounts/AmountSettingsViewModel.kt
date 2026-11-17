package aktual.prefs.vm.amounts

import aktual.budget.model.Currency
import aktual.budget.model.DbMetadataKey.Companion.BudgetName
import aktual.budget.utils.BudgetLocalPreferences
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.BudgetFormatPreferences
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.collectAsState
import aktual.prefs.vm.launchAndSet
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class AmountSettingsViewModel
internal constructor(
  preferences: BudgetFormatPreferences,
  localPreferences: BudgetLocalPreferences,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  val state: StateFlow<AmountSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val numberFormat by collectAsState(preferences.numberFormat)
      val hideFraction by collectAsState(preferences.hideFraction)
      val currency by collectAsState(preferences.currency)
      val symbolPosition by collectAsState(preferences.symbolPosition)
      val spaceBetweenAmountAndSymbol by collectAsState(preferences.spaceBetweenAmountAndSymbol)
      val budgetName by remember {
        localPreferences.observe(BudgetName)
      }
        .collectAsState(localPreferences[BudgetName])
      AmountSettingsState(
        budgetName = budgetName.orEmpty(),
        numberFormat =
          ListPreference(
            value = numberFormat,
            onChange = { launchAndSet(preferences.numberFormat, it) },
          ),
        hideFraction =
          BooleanPreference(
            value = hideFraction,
            onChange = { launchAndSet(preferences.hideFraction, it) },
          ),
        currency =
          ListPreference(
            value = currency,
            options = CurrencyOptions,
            onChange = { viewModelScope.launch { preferences.changeCurrency(it) } },
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

private val CurrencyOptions = Currency.entries.filter { it != Unknown }.toImmutableList()
