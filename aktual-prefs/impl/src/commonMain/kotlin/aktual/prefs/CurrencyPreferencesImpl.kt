package aktual.prefs

import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class CurrencyPreferencesImpl(dataStore: DataStore<Preferences>) : CurrencyPreferences {
  override val currency: Preference<Currency> =
    dataStore
      .translated(
        key = intPreferencesKey("currency"),
        default = DefaultCurrency,
        translator = enumOrdinalTranslator(),
      )
      .required()

  override val symbolPosition: Preference<CurrencySymbolPosition> =
    dataStore
      .translated(
        key = intPreferencesKey("currencySymbolPosition"),
        default = DefaultCurrency.symbolPosition,
        translator = enumOrdinalTranslator(),
      )
      .required()

  override val spaceBetweenAmountAndSymbol: Preference<Boolean> =
    dataStore
      .boolean(key = booleanPreferencesKey("currencySpaceBetweenAmountAndSymbol"), default = true)
      .required()

  private companion object {
    val DefaultCurrency = defaultCurrency()
  }
}
