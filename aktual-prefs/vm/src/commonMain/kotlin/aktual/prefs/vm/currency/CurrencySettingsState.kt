package aktual.prefs.vm.currency

import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import androidx.compose.runtime.Immutable

@Immutable
data class CurrencySettingsState(
  val currency: ListPreference<Currency>,
  val symbolPosition: ListPreference<CurrencySymbolPosition>,
  val spaceBetweenAmountAndSymbol: BooleanPreference,
)
