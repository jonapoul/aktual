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
    val isCustom: Boolean,
    val properties: ImmutableList<ThemeProperty>,
  ) : InspectThemeState
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

enum class PropertySorting {
  Default,
  ByName,
  ByColor,
}
