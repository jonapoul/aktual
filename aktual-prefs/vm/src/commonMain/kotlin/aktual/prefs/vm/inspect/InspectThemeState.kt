package aktual.prefs.vm.inspect

import aktual.core.model.ThemeId
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface InspectThemeState {
  data object Loading : InspectThemeState

  @JvmInline value class NotFound(val id: ThemeId) : InspectThemeState

  data class Loaded(
    val id: ThemeId,
    // Null for built-in themes, which go by their ID
    val name: String?,
    val source: ThemeSource,
    val properties: ImmutableList<ThemeProperty>,
  ) : InspectThemeState
}

enum class ThemeSource {
  BuiltIn,
  Downloaded,
  Local,
}

@Immutable
sealed interface InspectThemeEvent {
  @JvmInline value class Duplicated(val id: ThemeId) : InspectThemeEvent

  data object Deleted : InspectThemeEvent
}

@Immutable data class ThemeProperty(val name: String, val color: Color)

@Stable
@Suppress("MagicNumber")
fun Color.toHexString(): String {
  val r = (red * 255).roundToInt()
  val g = (green * 255).roundToInt()
  val b = (blue * 255).roundToInt()
  val a = (alpha * 255).roundToInt()
  return if (a == 255) {
    "#%02X%02X%02X".format(r, g, b)
  } else {
    "#%02X%02X%02X%02X".format(a, r, g, b)
  }
}

private val HexColorRegex = "#?([0-9A-Fa-f]{2})?[0-9A-Fa-f]{6}".toRegex()

// Inverse of toHexString: "#RRGGBB", or "#AARRGGBB" with alpha
@Suppress("MagicNumber")
fun String.parseHexColorOrNull(): Color? {
  val trimmed = trim()
  if (!HexColorRegex.matches(trimmed)) return null
  val hex = trimmed.removePrefix("#")
  val value = hex.toLong(radix = 16)
  return if (hex.length == 6) Color(value or 0xFF000000) else Color(value)
}

enum class PropertySorting {
  Default,
  ByName,
  ByColor,
}
