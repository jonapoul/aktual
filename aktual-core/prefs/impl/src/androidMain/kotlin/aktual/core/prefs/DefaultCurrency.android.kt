package aktual.core.prefs

import aktual.budget.model.Currency
import java.util.Locale
import java.util.Currency as JavaCurrency

internal actual fun defaultCurrency(): Currency {
  val code =
    try {
      JavaCurrency.getInstance(Locale.getDefault())?.currencyCode
    } catch (_: IllegalArgumentException) {
      null
    }
  return Currency.entries.firstOrNull { it.code == code } ?: Currency.UsDollar
}
