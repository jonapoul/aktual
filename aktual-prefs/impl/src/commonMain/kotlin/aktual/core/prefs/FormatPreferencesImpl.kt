package aktual.core.prefs

import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class FormatPreferencesImpl(dataStore: DataStore<Preferences>) : FormatPreferences {
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
}
