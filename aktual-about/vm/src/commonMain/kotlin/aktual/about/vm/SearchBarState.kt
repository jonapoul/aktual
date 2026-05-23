package aktual.about.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface SearchBarState {
  data object Visible : SearchBarState

  data object Gone : SearchBarState
}
