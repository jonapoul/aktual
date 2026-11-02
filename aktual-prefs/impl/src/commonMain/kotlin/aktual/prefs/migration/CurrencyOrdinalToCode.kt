package aktual.prefs.migration

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

internal class CurrencyOrdinalToCode : PreferenceKeyMigration<Int, String>(OldKey, NewKey) {
  override fun convert(old: Int): String? = LegacyCodes.getOrNull(old)

  companion object {
    val OldKey = intPreferencesKey("currency")
    val NewKey = stringPreferencesKey("currencyCode")

    // Currency codes in the enum's order back when the ordinal was stored. Frozen, so later changes
    // to the enum don't shift what old ordinals map to
    private val LegacyCodes =
      listOf(
        "",
        "AED",
        "ARS",
        "AUD",
        "BRL",
        "BYN",
        "CAD",
        "CHF",
        "CNY",
        "COP",
        "CRC",
        "CZK",
        "DKK",
        "EGP",
        "EUR",
        "GBP",
        "GTQ",
        "HKD",
        "HUF",
        "IDR",
        "INR",
        "JMD",
        "JPY",
        "KRW",
        "LKR",
        "MDL",
        "MYR",
        "PHP",
        "PLN",
        "QAR",
        "RON",
        "RSD",
        "RUB",
        "SAR",
        "SEK",
        "SGD",
        "THB",
        "TRY",
        "UAH",
        "USD",
        "UZS",
        "VND",
      )
  }
}
