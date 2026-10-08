package aktual.prefs

import aktual.di.AppScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class TransactionPreferencesImpl(dataStore: DataStore<Preferences>) : TransactionPreferences {
  override val alternateRowColours: Preference<Boolean> =
    dataStore
      .boolean(key = booleanPreferencesKey("alternateTransactionRowColours"), default = false)
      .required()
}
