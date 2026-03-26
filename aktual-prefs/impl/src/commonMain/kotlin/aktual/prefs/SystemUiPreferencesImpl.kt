package aktual.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class SystemUiPreferencesImpl(dataStore: DataStore<Preferences>) : SystemUiPreferences {
  override val showBottomBar: Preference<Boolean> =
    dataStore.boolean(key = booleanPreferencesKey("showBottomBar"), default = true).required()

  override val blurAppBars: Preference<Boolean> =
    dataStore.boolean(key = booleanPreferencesKey("blurAppBars"), default = true).required()

  override val blurDialogs: Preference<Boolean> =
    dataStore.boolean(key = booleanPreferencesKey("blurDialogs"), default = true).required()

  override val blurRadius: Preference<Float> =
    dataStore.float(key = floatPreferencesKey("blurRadius"), default = 5f).required()

  override val blurAlpha: Preference<Float> =
    dataStore.float(key = floatPreferencesKey("blurAlpha"), default = 0.35f).required()
}
