package aktual.core.ui

import aktual.budget.model.SyncState
import aktual.core.model.PingState
import androidx.compose.runtime.Immutable

@Immutable
sealed interface BottomBarState {
  data object Hidden : BottomBarState

  @Immutable
  data class Visible(val pingState: PingState, val syncState: SyncState, val budgetName: String?) :
    BottomBarState
}
