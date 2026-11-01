package aktual.prefs

import aktual.di.AppScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class SecurityPreferencesImpl(dataStore: DataStore<Preferences>) : SecurityPreferences {
  override val appLockEnabled: Preference<Boolean> =
    dataStore.boolean(key = booleanPreferencesKey("appLockEnabled"), default = false).required()

  override val appLockTimeout: Preference<AppLockTimeout> =
    dataStore
      .translated(
        key = intPreferencesKey("appLockTimeout"),
        default = AppLockTimeout.Default,
        translator = enumOrdinalTranslator(),
      )
      .required()

  override val hidePreviewInAppSwitcher: Preference<Boolean> =
    dataStore
      .boolean(key = booleanPreferencesKey("hidePreviewInAppSwitcher"), default = true)
      .required()
}
