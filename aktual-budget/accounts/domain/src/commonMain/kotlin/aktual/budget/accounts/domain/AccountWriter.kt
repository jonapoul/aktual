package aktual.budget.accounts.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.AccountGroupDao
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.DatabaseTables.ACCOUNT_GROUPS
import aktual.budget.db.dao.DatabaseTables.NOTES
import aktual.budget.db.dao.PayeeDao
import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.budget.model.Amount.Companion.Zero
import aktual.budget.model.CategoryId
import aktual.budget.model.LocalChange
import aktual.budget.model.SORT_INCREMENT
import aktual.budget.model.localChange
import aktual.budget.model.shoveSortOrders
import aktual.budget.model.tombstone
import aktual.budget.transactions.domain.NewTransaction
import aktual.budget.transactions.domain.Patch
import aktual.budget.transactions.domain.Patch.Keep
import aktual.budget.transactions.domain.Patch.To
import aktual.budget.transactions.domain.TransactionWriter
import aktual.core.utils.Calendar
import aktual.core.utils.UuidGenerator
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first

/**
 * Account and account group edits, as the handlers in packages/loot-core/src/server/accounts/app.ts
 * and account-groups/app.ts. Bank sync unlinking is left to the caller, see AccountDeleter.
 */
@Inject
class AccountWriter
internal constructor(
  private val writer: TransactionWriter,
  private val syncController: BudgetSyncController,
  private val accountDao: AccountDao,
  private val groupDao: AccountGroupDao,
  private val payeeDao: PayeeDao,
  private val uuidGenerator: UuidGenerator,
  private val calendar: Calendar,
) {
  suspend fun rename(id: AccountId, name: String) = writer.write { renameAccount(id, name) }

  // Account notes live in the notes table under "account-<id>"
  suspend fun setNotes(id: AccountId, notes: String) =
    sync(localChange(NOTES, "account-${id.value}", "note", notes))

  suspend fun setGroup(id: AccountId, group: AccountGroupId?) =
    sync(localChange(ACCOUNTS, id.value, GROUP_ID, group?.value))

  suspend fun reopen(id: AccountId) = sync(localChange(ACCOUNTS, id.value, "closed", false))

  /**
   * closeAccount() without forcing it. An account with no transactions is deleted instead. Any
   * balance goes to [transferTo] in a closing transfer dated today, categorised as [category] when
   * that's needed. Does nothing to an account that's missing, deleted or already closed.
   */
  suspend fun close(
    id: AccountId,
    transferTo: AccountId? = null,
    category: CategoryId? = null,
  ): CloseResult {
    val account = accountDao[id]
    if (account == null || account.tombstone == true || account.closed == true) return Unchanged

    val properties = accountDao.properties(id)
    if (properties.transactionCount == 0L) {
      sync(tombstone(ACCOUNTS, id.value))
      return Deleted
    }

    val balance = properties.balance
    require(balance == Zero || transferTo != null) { "A non-zero balance needs a transfer account" }
    require(transferTo != id) { "Can't transfer the balance to the account being closed" }

    writer.write(runTransfers = true) {
      add(listOf(localChange(ACCOUNTS, id.value, "closed", true)))
      if (balance != Zero && transferTo != null) {
        val payee =
          checkNotNull(payeeDao.transferPayee(transferTo)) { "No transfer payee for $transferTo" }
        val transaction =
          NewTransaction(
            account = id,
            date = calendar.today(),
            amount = -balance,
            payee = payee,
            category = category,
            notes = CLOSING_NOTE,
          )
        insert(transaction)
      }
    }
    return Closed
  }

  /**
   * moveAccount(): puts [id] just before [target], or at the end without one, among the open
   * accounts on the same side of the budget, or among closed accounts if it's closed. [group] moves
   * it into another group too, null being ungrouped.
   */
  suspend fun move(id: AccountId, target: AccountId?, group: Patch<AccountGroupId?> = Keep) {
    val rows = accountDao.observeManageList().first()
    val account = checkNotNull(rows.firstOrNull { it.id == id }) { "Account $id not found" }
    val siblings = rows.filter { row ->
      if (account.closed == true) {
        row.closed == true
      } else {
        row.offbudget == account.offbudget
      }
    }
    val shoved = shoveSortOrders(siblings.map { it.id to (it.sort_order ?: 0.0) }, target)
    val changes = buildList {
      shoved.updates.forEach { (other, order) -> add(sortOrder(ACCOUNTS, other.value, order)) }
      add(sortOrder(ACCOUNTS, id.value, shoved.sortOrder))
      if (group is To) add(localChange(ACCOUNTS, id.value, GROUP_ID, group.value?.value))
    }
    sync(changes)
  }

  /** insertAccountGroup(): a group after the last one. Names are unique, ignoring case. */
  suspend fun createGroup(name: String): AccountGroupId {
    checkGroupName(name, except = null)
    val id = uuidGenerator(::AccountGroupId)
    val sortOrder = (groupDao.maxSortOrder() ?: 0.0) + SORT_INCREMENT
    sync(
      listOf(
        localChange(ACCOUNT_GROUPS, id.value, "name", name),
        sortOrder(ACCOUNT_GROUPS, id.value, sortOrder),
      ),
    )
    return id
  }

  suspend fun renameGroup(id: AccountGroupId, name: String) {
    checkGroupName(name, except = id)
    sync(localChange(ACCOUNT_GROUPS, id.value, "name", name))
  }

  /** deleteAccountGroup(): its accounts become ungrouped. */
  suspend fun deleteGroup(id: AccountGroupId) {
    val changes = buildList {
      val nullValue: String? = null
      accountDao.idsInGroup(id).forEach {
        add(localChange(ACCOUNTS, it.value, GROUP_ID, nullValue))
      }
      add(tombstone(ACCOUNT_GROUPS, id.value))
    }
    sync(changes)
  }

  /** moveAccountGroup(): puts [id] just before [target], or at the end without one. */
  suspend fun moveGroup(id: AccountGroupId, target: AccountGroupId?) {
    val groups = groupDao.getAllActive().map { it.id to (it.sort_order ?: 0.0) }
    val shoved = shoveSortOrders(groups, target)
    val changes = buildList {
      shoved.updates.forEach { (other, order) ->
        add(sortOrder(ACCOUNT_GROUPS, other.value, order))
      }
      add(sortOrder(ACCOUNT_GROUPS, id.value, shoved.sortOrder))
    }
    sync(changes)
  }

  private suspend fun checkGroupName(name: String, except: AccountGroupId?) {
    val existing =
      groupDao.getAllActive().firstOrNull { item ->
        item.id != except && item.name.equals(name, ignoreCase = true)
      }
    if (existing != null) throw GroupNameTakenException(existing.name.orEmpty())
  }

  private suspend fun sync(change: LocalChange) = sync(listOf(change))

  private suspend fun sync(changes: List<LocalChange>) = syncController.syncChanges(changes)

  private fun sortOrder(dataset: String, row: String, order: Double) =
    localChange(dataset, row, "sort_order", order)

  private companion object {
    const val GROUP_ID = "account_group_id"
    const val CLOSING_NOTE = "Closing account"
  }
}

enum class CloseResult {
  Closed,
  Deleted,
  Unchanged,
}

class GroupNameTakenException(existing: String) :
  IllegalArgumentException("An '$existing' account group already exists")
