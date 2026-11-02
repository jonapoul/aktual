package aktual.prefs.migration

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences

// Run in order whenever the DataStore opens. Append new migrations to the end
fun preferenceMigrations(): List<DataMigration<Preferences>> = listOf(CurrencyOrdinalToCode())
