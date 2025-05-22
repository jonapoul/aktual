package actual.about.info.vm

import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko

@Immutable
sealed interface CheckUpdatesState {
  data object Inactive : CheckUpdatesState
  data object Checking : CheckUpdatesState
  data object NoUpdateFound : CheckUpdatesState
  @Poko class Failed(val cause: String) : CheckUpdatesState
  @Poko class UpdateFound(val version: String, val url: String) : CheckUpdatesState
}
