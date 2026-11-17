package aktual.prefs.migration

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

// Run in order whenever the DataStore opens. Append new migrations to the end
fun preferenceMigrations(): List<DataMigration<Preferences>> = listOf(RemoveKeys(BudgetFormatKeys))

// Formatting that moved into the budget's synced prefs. The local values are dropped rather than
// copied in, so they don't overwrite what the budget already uses on other devices
private val BudgetFormatKeys =
  listOf(
    intPreferencesKey("currency"),
    stringPreferencesKey("currencyCode"),
    intPreferencesKey("currencySymbolPosition"),
    booleanPreferencesKey("currencySpaceBetweenAmountAndSymbol"),
    booleanPreferencesKey("hideFraction"),
    stringPreferencesKey("dateFormat"),
    intPreferencesKey("firstDayOfWeekIdx"),
    intPreferencesKey("numberFormat"),
  )
