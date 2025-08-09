package actual.app.android

import actual.core.model.PingState
import androidx.compose.runtime.Immutable

@Immutable
sealed interface BottomBarState {
  data object Hidden : BottomBarState

  data class Visible(
    val pingState: PingState,
    val budgetName: String?,
  ) : BottomBarState
}
