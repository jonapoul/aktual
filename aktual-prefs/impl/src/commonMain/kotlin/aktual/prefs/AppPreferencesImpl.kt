package aktual.prefs

import aktual.core.model.ServerUrl
import aktual.core.model.Token
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class AppPreferencesImpl(dataStore: DataStore<Preferences>) : AppPreferences {
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

  override val isPrivacyEnabled: Preference<Boolean> =
    dataStore.boolean(key = booleanPreferencesKey("isPrivacyEnabled"), default = false).required()

  override val mostRecentNumBudgets: Preference<Int> =
    dataStore.int(key = intPreferencesKey("mostRecentNumBudgets"), default = 3).required()
}
