package aktual.budget.accounts.vm.manage

import aktual.budget.accounts.domain.AccountUndo
import kotlin.jvm.JvmInline

sealed interface ManageAccountsEvent {
  // A null group is Ungrouped
  data class AccountMoved(val name: String, val group: String?, val undo: AccountUndo) :
    ManageAccountsEvent

  data class GroupMoved(val name: String, val undo: AccountUndo) : ManageAccountsEvent

  data class Reopened(val name: String, val undo: AccountUndo) : ManageAccountsEvent

  data class GroupRenamed(val from: String, val to: String, val undo: AccountUndo) :
    ManageAccountsEvent

  data class GroupDeleted(val name: String, val undo: AccountUndo) : ManageAccountsEvent

  @JvmInline value class GroupNameTaken(val name: String) : ManageAccountsEvent

  @JvmInline value class ActionFailed(val message: String) : ManageAccountsEvent
}
