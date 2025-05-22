package actual.about.licenses.vm

import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko

@Immutable
sealed interface SearchBarState {
  @Poko class Visible(val text: String) : SearchBarState
  data object Gone : SearchBarState
}
