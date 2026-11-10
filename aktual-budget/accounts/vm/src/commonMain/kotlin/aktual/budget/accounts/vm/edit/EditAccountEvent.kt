package aktual.budget.accounts.vm.edit

import aktual.budget.accounts.domain.AccountUndo

sealed interface EditAccountEvent {
  // [renamedFrom] is the old name when the save renamed the account
  data class Saved(val name: String, val renamedFrom: String?, val undo: AccountUndo) :
    EditAccountEvent
}
