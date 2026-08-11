package aktual.prefs

import aktual.budget.model.BarEffect
import aktual.di.AppScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class SystemUiPreferencesImpl(dataStore: DataStore<Preferences>) : SystemUiPreferences {
  override val showBottomBar: Preference<Boolean> =
    dataStore.boolean(key = booleanPreferencesKey("showBottomBar"), default = true).required()

  override val appBarEffect: Preference<BarEffect> =
    dataStore
      .translated(
        key = intPreferencesKey("appBarEffect"),
        default = BarEffect.Default,
        translator = enumOrdinalTranslator(),
      )
      .required()

  override val hazeDialogs: Preference<Boolean> =
    dataStore.boolean(key = booleanPreferencesKey("blurDialogs"), default = true).required()

  override val hazeRadius: Preference<Float> =
    dataStore.float(key = floatPreferencesKey("blurRadius"), default = 5f).required()

  override val hazeAlpha: Preference<Float> =
    dataStore.float(key = floatPreferencesKey("blurAlpha"), default = 0.35f).required()

  override val hidePreviewInAppSwitcher: Preference<Boolean> =
    dataStore
      .boolean(key = booleanPreferencesKey("hidePreviewInAppSwitcher"), default = true)
      .required()
}
