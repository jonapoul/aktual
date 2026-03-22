package aktual.core.prefs

import aktual.core.theme.DarkTheme
import aktual.core.theme.Theme
import aktual.core.theme.ThemePreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class ThemePreferencesImpl(dataStore: DataStore<Preferences>) : ThemePreferences {
  override val useSystemDefault: Preference<Boolean> =
    dataStore.boolean(key = booleanPreferencesKey("theme.systemDefault"), default = true).required()

  override val nightTheme: Preference<Theme.Id> =
    dataStore
      .translated(
        key = stringPreferencesKey("theme.nightTheme"),
        default = DarkTheme.id,
        translator = ThemeIdTranslator,
      )
      .required()

  override val constantTheme: NullablePreference<Theme.Id> =
    dataStore.translated(
      key = stringPreferencesKey("theme.customTheme"),
      default = null,
      translator = ThemeIdTranslator,
    )

  private object ThemeIdTranslator : Translator<String, Theme.Id> {
    override fun encode(value: Theme.Id): String = value.value

    override fun decode(value: String): Theme.Id = Theme.Id(value)
  }
}
