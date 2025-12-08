package aktual.about.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface SearchBarState {
  data class Visible(val text: String) : SearchBarState
  data object Gone : SearchBarState
}
