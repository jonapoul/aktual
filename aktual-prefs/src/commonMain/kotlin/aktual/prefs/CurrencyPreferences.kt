package aktual.prefs

import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition

interface CurrencyPreferences {
  val currency: Preference<Currency>
  val symbolPosition: Preference<CurrencySymbolPosition>
  val spaceBetweenAmountAndSymbol: Preference<Boolean>
}
