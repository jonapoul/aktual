package aktual.prefs

import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormat

/**
 * Formatting stored in the open budget and synced to every device using it, matching upstream's
 * synced prefs. Only available in budget scope.
 */
interface BudgetFormatPreferences {
  val numberFormat: Preference<NumberFormat>
  val hideFraction: Preference<Boolean>
  val currency: Preference<Currency>
  val symbolPosition: Preference<CurrencySymbolPosition>
  val spaceBetweenAmountAndSymbol: Preference<Boolean>
  val dateFormat: Preference<DateFormat>
  val firstDayOfWeek: Preference<FirstDayOfWeek>
  val isPrivacyEnabled: Preference<Boolean>

  // Also resets the number format, decimals, symbol position and spacing to suit the new currency,
  // like upstream does
  suspend fun changeCurrency(currency: Currency)
}
