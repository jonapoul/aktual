package aktual.settings.vm.inspect

import aktual.core.theme.Theme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface InspectThemeState {
  data object Loading : InspectThemeState

  @JvmInline value class NotFound(val id: Theme.Id) : InspectThemeState

  data class Loaded(val id: Theme.Id, val properties: ImmutableList<ThemeProperty>) :
    InspectThemeState
}

@Immutable data class ThemeProperty(val name: String, val color: Color)
