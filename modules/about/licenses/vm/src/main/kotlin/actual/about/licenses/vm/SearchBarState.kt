package actual.about.licenses.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface SearchBarState {
  data class Visible(val text: String) : SearchBarState
  data object Gone : SearchBarState
}
