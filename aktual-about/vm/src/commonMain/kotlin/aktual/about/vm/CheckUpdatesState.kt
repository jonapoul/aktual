package aktual.about.vm

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
sealed interface CheckUpdatesState {
  @Serializable data object Inactive : CheckUpdatesState

  @Serializable data object Checking : CheckUpdatesState

  @Serializable data object NoUpdateFound : CheckUpdatesState

  @Serializable data class Failed(val cause: String) : CheckUpdatesState

  @Serializable data class UpdateFound(val version: String, val url: String) : CheckUpdatesState
}
