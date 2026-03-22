package aktual.budget.sync.vm

import aktual.core.model.Password
import androidx.compose.runtime.Immutable

@Immutable
sealed interface KeyPasswordState {
  data class Active(val input: Password) : KeyPasswordState

  data object Inactive : KeyPasswordState
}
