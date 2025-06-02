package actual.android.app

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface StatusBarState {
  data object Hidden : StatusBarState

  data class Visible(
    val isConnected: Boolean,
  ) : StatusBarState
}
