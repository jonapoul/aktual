package aktual.budget.sync.vm

import aktual.core.model.Password

sealed interface KeyPasswordState {
  data class Active(val input: Password) : KeyPasswordState
  data object Inactive : KeyPasswordState
}
