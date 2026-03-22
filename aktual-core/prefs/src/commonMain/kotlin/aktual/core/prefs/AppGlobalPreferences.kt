package aktual.core.prefs

import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormat
import aktual.core.model.ServerUrl
import aktual.core.model.Token

/** Prefs which are kept on this device, but apply across all budgets */
@Suppress("ComplexInterface") // TODO #983
interface AppGlobalPreferences {
  val token: NullablePreference<Token>
  val serverUrl: NullablePreference<ServerUrl>
  val showBottomBar: Preference<Boolean>
  val hideFraction: Preference<Boolean>
  val dateFormat: Preference<DateFormat>
  val firstDayOfWeek: Preference<FirstDayOfWeek>
  val numberFormat: Preference<NumberFormat>
  val currency: Preference<Currency>
  val currencySymbolPosition: Preference<CurrencySymbolPosition>
  val currencySpaceBetweenAmountAndSymbol: Preference<Boolean>
  val isPrivacyEnabled: Preference<Boolean>
  val blurTopBar: Preference<Boolean>
  val blurStatusBar: Preference<Boolean>
  val blurDialogs: Preference<Boolean>
  val blurRadius: Preference<Float>
  val blurAlpha: Preference<Float>
}
