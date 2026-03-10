package aktual.settings.vm.root

import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormat
import aktual.settings.vm.BooleanPreference
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class SettingsScreenState(
  val showBottomBar: BooleanPreference,
  val numberFormat: NumberFormatPreference,
  val hideFraction: BooleanPreference,
  val dateFormat: DateFormat,
  val firstDayOfWeek: FirstDayOfWeek,
  val currency: CurrencyPreference,
  val currencySymbolPosition: CurrencySymbolPositionPreference,
  val currencySpaceBetweenAmountAndSymbol: BooleanPreference,
)

@Immutable
@JvmInline
value class NumberFormatPreference(val selected: NumberFormat) {
  companion object {
    val Values = NumberFormat.entries.toImmutableList()
  }
}

@Immutable
data class CurrencyPreference(val selected: Currency) {
  companion object {
    val Values = Currency.entries.toImmutableList()
  }
}

@Immutable
data class CurrencySymbolPositionPreference(
  val selected: CurrencySymbolPosition,
  val enabled: Boolean,
) {
  companion object {
    val Values = CurrencySymbolPosition.entries.toImmutableList()
  }
}
