package aktual.budget.accounts.ui.manage

import aktual.budget.accounts.vm.manage.AccountGroupItem
import aktual.budget.accounts.vm.manage.ManagedAccount
import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline

internal sealed interface ManageAccountsAction

internal data object NavigateBack : ManageAccountsAction

internal data object Retry : ManageAccountsAction

internal data object ToggleClosed : ManageAccountsAction

internal data object NewAccount : ManageAccountsAction

@JvmInline internal value class Show(val overlay: ManageOverlay) : ManageAccountsAction

@JvmInline internal value class Reopen(val id: AccountId) : ManageAccountsAction

@JvmInline internal value class ViewTransactions(val id: AccountId) : ManageAccountsAction

@JvmInline internal value class CreateGroup(val name: String) : ManageAccountsAction

internal data class RenameGroup(val id: AccountGroupId, val name: String) : ManageAccountsAction

@JvmInline internal value class DeleteGroup(val id: AccountGroupId) : ManageAccountsAction

// The sheet or dialog shown over the list
@Immutable internal sealed interface ManageOverlay

@JvmInline internal value class AccountMenu(val account: ManagedAccount) : ManageOverlay

@JvmInline internal value class EditAccount(val id: AccountId) : ManageOverlay

@JvmInline internal value class CloseAccount(val id: AccountId) : ManageOverlay

@JvmInline internal value class DeleteAccount(val id: AccountId) : ManageOverlay

internal data object CreateGroupDialog : ManageOverlay

@JvmInline internal value class RenameGroupDialog(val group: AccountGroupItem) : ManageOverlay

internal data class DeleteGroupDialog(val group: AccountGroupItem, val accountCount: Int) :
  ManageOverlay
