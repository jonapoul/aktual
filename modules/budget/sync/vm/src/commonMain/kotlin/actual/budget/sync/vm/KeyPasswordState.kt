package actual.budget.sync.vm

import actual.core.model.Password

sealed interface KeyPasswordState {
  data class Active(val input: Password) : KeyPasswordState
  data object Inactive : KeyPasswordState
}
