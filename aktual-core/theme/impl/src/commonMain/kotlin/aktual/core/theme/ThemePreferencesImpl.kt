package aktual.core.theme

import dev.jonpoulton.preferences.core.NullableStringSerializer
import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.Preferences
import dev.jonpoulton.preferences.core.StringSerializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class ThemePreferencesImpl(preferences: Preferences) : ThemePreferences {
  override val useSystemDefault: Preference<Boolean> =
    preferences.getBoolean(key = "theme.systemDefault", default = true)

  override val nightTheme: Preference<Theme.Id> =
    preferences.getObject(
      key = "theme.nightTheme",
      serializer = ThemeIdSerializer,
      default = DarkTheme.id,
    )

  override val constantTheme: Preference<Theme.Id?> =
    preferences.getNullableObject(
      key = "theme.customTheme",
      serializer = NullableThemeIdSerializer,
      default = null,
    )

  private object ThemeIdSerializer : StringSerializer<Theme.Id> {
    override fun deserialize(value: String): Theme.Id = Theme.Id(value)

    override fun serialize(value: Theme.Id): String = value.value
  }

  private object NullableThemeIdSerializer : NullableStringSerializer<Theme.Id> {
    override fun deserialize(value: String?): Theme.Id? = value?.let(Theme::Id)

    override fun serialize(value: Theme.Id?): String? = value?.value
  }
}
