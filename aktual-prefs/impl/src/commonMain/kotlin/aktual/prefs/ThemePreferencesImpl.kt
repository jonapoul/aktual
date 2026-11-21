package aktual.prefs

import aktual.core.model.ThemeId
import aktual.di.AppScope
import aktual.theme.DarkColors
import aktual.theme.LightColors
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class ThemePreferencesImpl(dataStore: DataStore<Preferences>) : ThemePreferences {
  override val useSystemDefault: Preference<Boolean> =
    dataStore.boolean(key = booleanPreferencesKey("theme.systemDefault"), default = true).required()

  override val dayTheme: Preference<ThemeId> =
    dataStore
      .translated(
        key = stringPreferencesKey("theme.dayTheme"),
        default = LightColors.id,
        translator = ThemeIdTranslator,
      )
      .required()

  override val nightTheme: Preference<ThemeId> =
    dataStore
      .translated(
        key = stringPreferencesKey("theme.nightTheme"),
        default = DarkColors.id,
        translator = ThemeIdTranslator,
      )
      .required()

  override val constantTheme: NullablePreference<ThemeId> =
    dataStore.translated(
      key = stringPreferencesKey("theme.customTheme"),
      default = null,
      translator = ThemeIdTranslator,
    )

  private object ThemeIdTranslator : Translator<String, ThemeId> {
    override fun encode(value: ThemeId): String = value.value

    override fun decode(value: String): ThemeId = ThemeId(value)
  }
}
