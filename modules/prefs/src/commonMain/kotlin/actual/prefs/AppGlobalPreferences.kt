package actual.prefs

import actual.account.model.LoginToken
import actual.core.model.ColorSchemeType
import actual.core.model.DarkColorSchemeType
import actual.core.model.ServerUrl
import dev.jonpoulton.preferences.core.IntSerializer
import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.Preferences
import dev.jonpoulton.preferences.core.SimpleNullableStringSerializer
import javax.inject.Inject

/**
 * Prefs which are kept on this device, but apply across all budgets
 */
class AppGlobalPreferences @Inject constructor(preferences: Preferences) {
  val loginToken: Preference<LoginToken?> = preferences
    .getNullableObject(key = "token", LoginTokenSerializer, default = null)

  val colorSchemeType: Preference<ColorSchemeType> = preferences
    .getObject(key = "colorSchemeType", ColorSchemeSerializer, default = ColorSchemeType.System)

  val darkColorSchemeType: Preference<DarkColorSchemeType> = preferences
    .getObject(key = "darkColorSchemeType", DarkColorSchemeSerializer, default = ColorSchemeType.Dark)

  val serverUrl: Preference<ServerUrl?> = preferences
    .getNullableObject(key = "serverUrl", ServerUrlSerializer, default = null)

  val showBottomBar: Preference<Boolean> = preferences.getBoolean(key = "bottomBar.show", default = true)

  private companion object {
    val ColorSchemeSerializer = object : IntSerializer<ColorSchemeType> {
      override fun deserialize(value: Int): ColorSchemeType = when (value) {
        0 -> ColorSchemeType.System
        1 -> ColorSchemeType.Light
        2 -> ColorSchemeType.Dark
        3 -> ColorSchemeType.Midnight
        else -> error("Unrecognised ColorSchemeType: '$value'")
      }

      override fun serialize(value: ColorSchemeType): Int = when (value) {
        ColorSchemeType.System -> 0
        ColorSchemeType.Light -> 1
        ColorSchemeType.Dark -> 2
        ColorSchemeType.Midnight -> 3
      }
    }

    val DarkColorSchemeSerializer = object : IntSerializer<DarkColorSchemeType> {
      override fun deserialize(value: Int): DarkColorSchemeType = when (value) {
        2 -> ColorSchemeType.Dark
        3 -> ColorSchemeType.Midnight
        else -> error("Unrecognised DarkColorSchemeType: '$value'")
      }

      override fun serialize(value: DarkColorSchemeType): Int = when (value) {
        ColorSchemeType.Dark -> 2
        ColorSchemeType.Midnight -> 3
      }
    }

    val LoginTokenSerializer = SimpleNullableStringSerializer { token -> token?.let(::LoginToken) }
    val ServerUrlSerializer = SimpleNullableStringSerializer { url -> url?.let(::ServerUrl) }
  }
}
