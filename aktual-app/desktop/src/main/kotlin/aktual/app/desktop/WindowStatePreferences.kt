package aktual.app.desktop

import aktual.core.prefs.JvmPreferences
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import dev.jonpoulton.preferences.core.IntSerializer
import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.StringSerializer
import dev.zacsweers.metro.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Inject
class WindowStatePreferences(private val preferences: JvmPreferences) {
  fun save(state: WindowState) {
    position.set(state.position)
    size.set(state.size)
    isMinimized.set(state.isMinimized)
    placement.set(state.placement)
    preferences.flush()
  }

  val position: Preference<WindowPosition> = preferences.getObject(
    key = "window.position",
    default = WindowPosition.PlatformDefault,
    serializer = WindowPositionSerializer,
  )

  val size: Preference<DpSize> = preferences.getObject(
    key = "window.size",
    default = DpSize(width = 800.dp, height = 600.dp),
    serializer = DpSizeSerializer,
  )

  val isMinimized: Preference<Boolean> = preferences.getBoolean(
    key = "window.isMinimized",
    default = false,
  )

  val placement: Preference<WindowPlacement> = preferences.getObject(
    key = "window.placement",
    default = WindowPlacement.Floating,
    serializer = WindowPlacementSerializer,
  )
}

@Serializable
private data class Position(val x: Float, val y: Float)

@Serializable
private data class Size(val width: Float, val height: Float)

private object WindowPositionSerializer : StringSerializer<WindowPosition> {
  override fun deserialize(value: String): WindowPosition = try {
    val position = Json.decodeFromString(Position.serializer(), value)
    WindowPosition.Absolute(position.x.dp, position.y.dp)
  } catch (_: SerializationException) {
    WindowPosition.PlatformDefault
  }

  override fun serialize(value: WindowPosition): String = when (value) {
    is WindowPosition.Absolute -> with(value) { Json.encodeToString(Position(x.value, y.value)) }
    else -> "PlatformDefault"
  }
}

private object DpSizeSerializer : StringSerializer<DpSize> {
  override fun deserialize(value: String): DpSize {
    val size = Json.decodeFromString(Size.serializer(), value)
    return DpSize(size.width.dp, size.height.dp)
  }

  override fun serialize(value: DpSize): String {
    val size = Size(value.width.value, value.height.value)
    return Json.encodeToString(size)
  }
}

private object WindowPlacementSerializer : IntSerializer<WindowPlacement> {
  override fun deserialize(value: Int): WindowPlacement = WindowPlacement.entries[value]
  override fun serialize(value: WindowPlacement): Int = value.ordinal
}
