package aktual.prefs.vm.inspect

import aktual.core.model.ThemeId
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
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
