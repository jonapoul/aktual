package aktual.prefs.vm.inspect.search

import aktual.prefs.vm.inspect.ThemeProperty
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface SearchThemeState {
  data object NoQuery : SearchThemeState

  data object NoResults : SearchThemeState

  data class Results(val query: String, val properties: ImmutableList<ThemeProperty>) :
    SearchThemeState
}
