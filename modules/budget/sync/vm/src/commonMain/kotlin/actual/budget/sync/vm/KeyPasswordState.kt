package actual.budget.sync.vm

import actual.account.model.Password

sealed interface KeyPasswordState {
  data class Active(val input: Password) : KeyPasswordState
  data object Inactive : KeyPasswordState
}
