package actual.android.app

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface BottomBarState {
  data object Hidden : BottomBarState

  data class Visible(
    val isConnected: Boolean,
    val budgetName: String?,
  ) : BottomBarState
}
