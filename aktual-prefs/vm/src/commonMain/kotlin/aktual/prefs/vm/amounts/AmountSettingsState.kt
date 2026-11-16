package aktual.prefs.vm.amounts

import aktual.budget.model.Currency
import aktual.budget.model.CurrencyConfig
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.NumberFormat
import aktual.budget.model.NumberFormatConfig
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import androidx.compose.runtime.Immutable

@Immutable
data class AmountSettingsState(
  val numberFormat: ListPreference<NumberFormat>,
  val hideFraction: BooleanPreference,
  val currency: ListPreference<Currency>,
  val symbolPosition: ListPreference<CurrencySymbolPosition>,
  val spaceBetweenAmountAndSymbol: BooleanPreference,
) {
  val numberFormatConfig: NumberFormatConfig
    get() = NumberFormatConfig(numberFormat.value, hideFraction.value)

  val currencyConfig: CurrencyConfig
    get() =
      CurrencyConfig(
        currency = currency.value,
        position = symbolPosition.value,
        includeSpace = spaceBetweenAmountAndSymbol.value,
      )
}
