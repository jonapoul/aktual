package aktual.core.prefs

import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormat
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import dev.jonpoulton.preferences.core.Preference

/** Prefs which are kept on this device, but apply across all budgets */
interface AppGlobalPreferences {
  val token: Preference<Token?>
  val serverUrl: Preference<ServerUrl?>
  val showBottomBar: Preference<Boolean>
  val hideFraction: Preference<Boolean>
  val firstDayOfWeek: Preference<FirstDayOfWeek>
  val numberFormat: Preference<NumberFormat>
  val currency: Preference<Currency>
  val currencySymbolPosition: Preference<CurrencySymbolPosition>
  val currencySpaceBetweenAmountAndSymbol: Preference<Boolean>
}
