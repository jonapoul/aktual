package aktual.prefs

import aktual.budget.model.BarEffect
import aktual.budget.model.BarGlassStyle
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

  override val barBlurRadius: Preference<Float> =
    dataStore.float(key = floatPreferencesKey("blurRadius"), default = 5f).required()

  override val barBlurAlpha: Preference<Float> =
    dataStore.float(key = floatPreferencesKey("blurAlpha"), default = 0.35f).required()

  override val dialogBlurRadius: Preference<Float> =
    dataStore.float(key = floatPreferencesKey("dialogBlurRadius"), default = 5f).required()

  override val dialogBlurAlpha: Preference<Float> =
    dataStore.float(key = floatPreferencesKey("dialogBlurAlpha"), default = 0.35f).required()

  override val glassStyle: Preference<BarGlassStyle> =
    dataStore
      .translated(
        key = intPreferencesKey("glassStyle"),
        default = BarGlassStyle.Default,
        translator = enumOrdinalTranslator(),
      )
      .required()

  override val glassCornerRadius: Preference<Float> =
    dataStore.float(key = floatPreferencesKey("glassCornerRadius"), default = 24f).required()
}
