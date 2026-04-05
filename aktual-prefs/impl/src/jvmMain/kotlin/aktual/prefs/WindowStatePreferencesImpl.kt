package aktual.prefs

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@ContributesBinding(AppScope::class)
class WindowStatePreferencesImpl(dataStore: DataStore<Preferences>) : WindowStatePreferences {
  override val position: Preference<WindowPosition> =
    dataStore
      .translated(
        key = stringPreferencesKey("window.position"),
        default = WindowPosition.PlatformDefault,
        translator = WindowPositionTranslator,
      )
      .required()

  override val size: Preference<DpSize> =
    dataStore
      .translated(
        key = stringPreferencesKey("window.size"),
        default = DefaultSize,
        translator = DpSizeTranslator,
      )
      .required()

  override val isMinimized: Preference<Boolean> =
    dataStore.boolean(key = booleanPreferencesKey("window.isMinimized"), default = false).required()

  override val placement: Preference<WindowPlacement> =
    dataStore
      .translated(
        key = intPreferencesKey("window.placement"),
        default = WindowPlacement.Floating,
        translator = enumOrdinalTranslator(),
      )
      .required()

  override suspend fun save(state: WindowState) {
    position.set(state.position)
    size.set(state.size)
    isMinimized.set(state.isMinimized)
    placement.set(state.placement)
  }

  private object WindowPositionTranslator : Translator<String, WindowPosition> {
    private const val PLATFORM_DEFAULT = "PlatformDefault"

    override fun encode(value: WindowPosition): String =
      if (value is WindowPosition.Absolute) {
        Json.encodeToString(Position(value.x.value, value.y.value))
      } else {
        PLATFORM_DEFAULT
      }

    override fun decode(value: String): WindowPosition =
      try {
        val pos = Json.decodeFromString<Position>(value)
        WindowPosition.Absolute(pos.x.dp, pos.y.dp)
      } catch (_: SerializationException) {
        WindowPosition.PlatformDefault
      }
  }

  private object DpSizeTranslator : Translator<String, DpSize> {
    override fun encode(value: DpSize): String =
      Json.encodeToString(Size(value.width.value, value.height.value))

    override fun decode(value: String): DpSize =
      try {
        val size = Json.decodeFromString<Size>(value)
        DpSize(size.width.dp, size.height.dp)
      } catch (_: SerializationException) {
        DefaultSize
      }
  }

  @Serializable private data class Position(val x: Float, val y: Float)

  @Serializable private data class Size(val width: Float, val height: Float)

  private companion object {
    val DefaultSize = DpSize(width = 800.dp, height = 600.dp)
  }
}
