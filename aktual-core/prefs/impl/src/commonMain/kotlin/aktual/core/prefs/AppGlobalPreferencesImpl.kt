package aktual.core.prefs

import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormat
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class AppGlobalPreferencesImpl(dataStore: DataStore<Preferences>) : AppGlobalPreferences {
  override val token: NullablePreference<Token> =
    dataStore.translated(
      key = stringPreferencesKey("token"),
      default = null,
      translator = toStringTranslator(::Token),
    )

  override val serverUrl: NullablePreference<ServerUrl> =
    dataStore.translated(
      key = stringPreferencesKey("serverUrl"),
      default = null,
      translator = toStringTranslator(::ServerUrl),
    )

  override val showBottomBar: Preference<Boolean> =
    dataStore.boolean(key = booleanPreferencesKey("showBottomBar"), default = true).required()

  override val hideFraction: Preference<Boolean> =
    dataStore.boolean(key = booleanPreferencesKey("hideFraction"), default = false).required()

  override val dateFormat: Preference<DateFormat> =
    dataStore
      .translated(
        key = stringPreferencesKey("dateFormat"),
        default = DateFormat.Default,
        translator = enumStringTranslator(),
      )
      .required()

  override val firstDayOfWeek: Preference<FirstDayOfWeek> =
    dataStore
      .translated(
        key = intPreferencesKey("firstDayOfWeekIdx"),
        default = FirstDayOfWeek.Default,
        translator = enumOrdinalTranslator(),
      )
      .required()

  override val numberFormat: Preference<NumberFormat> =
    dataStore
      .translated(
        key = intPreferencesKey("numberFormat"),
        default = NumberFormat.Default,
        translator = enumOrdinalTranslator(),
      )
      .required()

  override val currency: Preference<Currency> =
    dataStore
      .translated(
        key = intPreferencesKey("currency"),
        default = DefaultCurrency,
        translator = enumOrdinalTranslator(),
      )
      .required()

  override val currencySymbolPosition: Preference<CurrencySymbolPosition> =
    dataStore
      .translated(
        key = intPreferencesKey("currencySymbolPosition"),
        default = DefaultCurrency.symbolPosition,
        translator = enumOrdinalTranslator(),
      )
      .required()

  override val currencySpaceBetweenAmountAndSymbol: Preference<Boolean> =
    dataStore
      .boolean(key = booleanPreferencesKey("currencySpaceBetweenAmountAndSymbol"), default = true)
      .required()

  private companion object {
    val DefaultCurrency = defaultCurrency()
  }
}
