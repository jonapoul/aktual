package actual.android.app

import actual.core.model.PingState
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface BottomBarState {
  data object Hidden : BottomBarState

  data class Visible(
    val pingState: PingState,
    val budgetName: String?,
  ) : BottomBarState
}
