package aktual.budget.accounts.vm.manage

import aktual.budget.accounts.domain.AccountUndo
import aktual.budget.accounts.domain.AccountWriter
import aktual.budget.accounts.domain.GroupNameTakenException
import aktual.budget.db.GetManageList
import aktual.budget.db.accountGroups.GetAllActive
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.AccountGroupDao
import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.Amount.Companion.Zero
import aktual.budget.transactions.domain.Patch.Keep
import aktual.budget.transactions.domain.Patch.To
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetCoroutineScope
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Accounts and account groups, laid out as upstream's redesigned sidebar (useSidebarAccountTree.ts)
 * but keeping empty groups. Drags save on drop and send an event with their undo.
 */
@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ManageAccountsViewModel
internal constructor(
  private val writer: AccountWriter,
  accountDao: AccountDao,
  groupDao: AccountGroupDao,
  private val budgetScope: BudgetCoroutineScope,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  private val closedExpanded = MutableStateFlow(false)

  private val mutableEvents =
    MutableSharedFlow<ManageAccountsEvent>(extraBufferCapacity = 1, onBufferOverflow = DROP_OLDEST)
  val events: SharedFlow<ManageAccountsEvent> = mutableEvents.asSharedFlow()

  val state: StateFlow<ManageAccountsState> =
    retries
      .onStart { emit(Unit) }
      .flatMapLatest {
        combine(accountDao.observeManageList(), groupDao.observeAllActive(), closedExpanded) {
            accounts,
            groups,
            expanded ->
            buildState(accounts, groups, expanded)
          }
          .onStart<ManageAccountsState> { emit(Loading) }
          .catch { e ->
            log.e(e) { "Failed loading accounts" }
            emit(Failure(e.requireMessage()))
          }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = Loading)

  fun retry() {
    retries.tryEmit(Unit)
  }

  fun toggleClosed() = closedExpanded.update { !it }

  /**
   * Drops [id] into [group] just before [before], or at the end of the group without one. Only open
   * accounts move, and only within their side of the budget.
   */
  fun moveAccount(id: AccountId, group: AccountGroupId?, before: AccountId?) {
    val success = state.value as? Success ?: return
    val account = success.openAccounts().firstOrNull { it.id == id } ?: return
    if (before == id) return
    val groupName = group?.let { g -> success.groups.firstOrNull { it.id == g }?.name }
    if (group != null && groupName == null) return
    val patch = if (group == account.group) Keep else To(group)
    launchAction("moving account $id") {
      val undo = writer.move(id, before, patch)
      mutableEvents.tryEmit(ManageAccountsEvent.AccountMoved(account.name, groupName, undo))
    }
  }

  // Puts [id] just before [before], or last without one
  fun moveGroup(id: AccountGroupId, before: AccountGroupId?) {
    val success = state.value as? Success ?: return
    val group = success.groups.firstOrNull { it.id == id } ?: return
    if (before == id) return
    launchAction("moving group $id") {
      val undo = writer.moveGroup(id, before)
      mutableEvents.tryEmit(ManageAccountsEvent.GroupMoved(group.name, undo))
    }
  }

  fun createGroup(name: String) {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return
    launchAction("creating group $trimmed") { writer.createGroup(trimmed) }
  }

  fun renameGroup(id: AccountGroupId, name: String) {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return
    val success = state.value as? Success ?: return
    val group = success.groups.firstOrNull { it.id == id } ?: return
    if (group.name == trimmed) return
    launchAction("renaming group $id") {
      val undo = writer.renameGroup(id, trimmed)
      mutableEvents.tryEmit(ManageAccountsEvent.GroupRenamed(group.name, trimmed, undo))
    }
  }

  fun deleteGroup(id: AccountGroupId) {
    val success = state.value as? Success ?: return
    val group = success.groups.firstOrNull { it.id == id } ?: return
    launchAction("deleting group $id") {
      val undo = writer.deleteGroup(id)
      mutableEvents.tryEmit(ManageAccountsEvent.GroupDeleted(group.name, undo))
    }
  }

  fun reopen(id: AccountId) {
    val success = state.value as? Success ?: return
    val account = success.closed.firstOrNull { it.id == id } ?: return
    launchAction("reopening account $id") {
      val undo = writer.reopen(id)
      mutableEvents.tryEmit(ManageAccountsEvent.Reopened(account.name, undo))
    }
  }

  // The undo snackbar can outlive this screen, so the undo can't be tied to viewModelScope
  fun undo(undo: AccountUndo) {
    budgetScope.launch {
      runCatchingCancellable(
        attempt = { writer.undo(undo) },
        onFailure = { e ->
          log.e(e) { "Failed undoing account change" }
          mutableEvents.tryEmit(ManageAccountsEvent.ActionFailed(e.requireMessage()))
        },
      )
    }
  }

  private fun launchAction(description: String, action: suspend () -> Unit) {
    viewModelScope.launch {
      runCatchingCancellable(
        attempt = { action() },
        onFailure = { e ->
          log.e(e) { "Failed $description" }
          val event =
            if (e is GroupNameTakenException) {
              ManageAccountsEvent.GroupNameTaken(e.existing)
            } else {
              ManageAccountsEvent.ActionFailed(e.requireMessage())
            }
          mutableEvents.tryEmit(event)
        },
      )
    }
  }

  private fun Success.openAccounts() = (onBudget.groups + offBudget.groups).flatMap { it.accounts }

  private fun buildState(
    rows: List<GetManageList>,
    groupRows: List<GetAllActive>,
    expanded: Boolean,
  ): Success {
    val groups = groupRows.map { AccountGroupItem(it.id, it.name.orEmpty()) }
    val accounts = rows.map { it.toAccount() }
    val (closed, open) = rows.zip(accounts).partition { (row, _) -> row.closed == true }
    val openAccounts = open.map { it.second }
    return Success(
      onBudget = section(openAccounts.filter { !it.offBudget }, groups),
      offBudget = section(openAccounts.filter { it.offBudget }, groups),
      groups = groups.toImmutableList(),
      closed = closed.map { it.second }.toImmutableList(),
      closedExpanded = expanded,
    )
  }

  private fun section(accounts: List<ManagedAccount>, groups: List<AccountGroupItem>) =
    AccountSection(
      total = accounts.total(),
      groups = buildList {
          add(group(id = null, name = "", accounts.filter { it.group == null }))
          groups.forEach { g -> add(group(g.id, g.name, accounts.filter { it.group == g.id })) }
        }
          .toImmutableList(),
    )

  private fun group(id: AccountGroupId?, name: String, accounts: List<ManagedAccount>) =
    ManagedGroup(id, name, accounts.total(), accounts.toImmutableList())

  private fun List<ManagedAccount>.total(): Amount = fold(Zero) { sum, a -> sum + a.balance }

  private fun GetManageList.toAccount() =
    ManagedAccount(
      id = id,
      name = name.orEmpty(),
      balance = Amount(balance),
      transactionCount = transaction_count,
      hasNotes = has_notes,
      offBudget = offbudget == true,
      group = account_group_id,
    )
}
