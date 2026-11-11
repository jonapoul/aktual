package aktual.budget.accounts.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.AccountGroupDao
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.DatabaseTables.ACCOUNT_GROUPS
import aktual.budget.db.dao.DatabaseTables.NOTES
import aktual.budget.db.dao.NotesDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.budget.model.Amount.Companion.Zero
import aktual.budget.model.CategoryId
import aktual.budget.model.LocalChange
import aktual.budget.model.SORT_INCREMENT
import aktual.budget.model.TransactionId
import aktual.budget.model.localChange
import aktual.budget.model.shoveSortOrders
import aktual.budget.model.tombstone
import aktual.budget.model.untombstone
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
class AccountWriter(
  private val writer: TransactionWriter,
  private val syncController: BudgetSyncController,
  private val accountDao: AccountDao,
  private val groupDao: AccountGroupDao,
  private val payeeDao: PayeeDao,
  private val notesDao: NotesDao,
  private val uuidGenerator: UuidGenerator,
  private val calendar: Calendar,
) {
  suspend fun rename(id: AccountId, name: String) = writer.write { renameAccount(id, name) }

  // Account notes live in the notes table under "account-<id>"
  suspend fun setNotes(id: AccountId, notes: String) = sync(notesChange(id, notes))

  suspend fun notes(id: AccountId): String = notesDao.getNote(notesId(id)).orEmpty()

  /**
   * The edit sheet's save: updateAccount() with a new name, plus its notes and group, in one batch.
   * Returns what puts them back.
   */
  suspend fun edit(
    id: AccountId,
    name: Patch<String> = Keep,
    notes: Patch<String> = Keep,
    group: Patch<AccountGroupId?> = Keep,
  ): AccountUndo {
    val account = checkNotNull(accountDao[id]) { "Account $id not found" }
    val oldNotes = if (notes is To) notes(id) else null
    val changes = mutableListOf<LocalChange>()
    val undo = mutableListOf<LocalChange>()
    if (name is To) {
      changes += localChange(ACCOUNTS, id.value, "name", name.value)
      undo += localChange(ACCOUNTS, id.value, "name", account.name)
    }
    if (notes is To) {
      changes += notesChange(id, notes.value)
      undo += notesChange(id, oldNotes.orEmpty())
    }
    if (group is To) {
      changes += localChange(ACCOUNTS, id.value, GROUP_ID, group.value?.value)
      undo += localChange(ACCOUNTS, id.value, GROUP_ID, account.account_group_id?.value)
    }
    if (changes.isNotEmpty()) sync(changes)
    return AccountUndo(undo)
  }

  suspend fun setGroup(id: AccountId, group: AccountGroupId?) =
    sync(localChange(ACCOUNTS, id.value, GROUP_ID, group?.value))

  // Returns what closes it again
  suspend fun reopen(id: AccountId): AccountUndo {
    sync(localChange(ACCOUNTS, id.value, CLOSED, false))
    return AccountUndo(listOf(localChange(ACCOUNTS, id.value, CLOSED, true)))
  }

  /**
   * closeAccount() without forcing it. An account with no transactions is deleted instead. Any
   * balance goes to [transferTo] in a closing transfer dated today, categorised as [category] when
   * that's needed. Does nothing to an account that's missing, deleted or already closed. Returns
   * what puts it back, the closing transfer removed.
   */
  suspend fun close(
    id: AccountId,
    transferTo: AccountId? = null,
    category: CategoryId? = null,
  ): CloseOutcome {
    val account = accountDao[id]
    if (account == null || account.tombstone == true || account.closed == true) {
      return CloseOutcome(Unchanged, AccountUndo(emptyList()))
    }

    val properties = accountDao.properties(id)
    if (properties.transactionCount == 0L) {
      sync(tombstone(ACCOUNTS, id.value))
      return CloseOutcome(Deleted, AccountUndo(listOf(untombstone(ACCOUNTS, id.value))))
    }

    val balance = properties.balance
    require(balance == Zero || transferTo != null) { "A non-zero balance needs a transfer account" }
    require(transferTo != id) { "Can't transfer the balance to the account being closed" }

    val closing =
      writer.write(runTransfers = true) {
        add(listOf(localChange(ACCOUNTS, id.value, CLOSED, true)))
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
        } else {
          null
        }
      }
    val undo =
      AccountUndo(listOf(localChange(ACCOUNTS, id.value, CLOSED, false)), setOfNotNull(closing))
    return CloseOutcome(Closed, undo)
  }

  /**
   * moveAccount(): puts [id] just before [target], or at the end without one, among the open
   * accounts on the same side of the budget, or among closed accounts if it's closed. [group] moves
   * it into another group too, null being ungrouped. Returns what puts it back.
   */
  suspend fun move(
    id: AccountId,
    target: AccountId?,
    group: Patch<AccountGroupId?> = Keep,
  ): AccountUndo {
    val rows = accountDao.observeManageList().first()
    val account = checkNotNull(rows.firstOrNull { it.id == id }) { "Account $id not found" }
    val siblings = rows.filter { row ->
      if (account.closed == true) {
        row.closed == true
      } else {
        row.offbudget == account.offbudget
      }
    }
    val orders = siblings.map { it.id to (it.sort_order ?: 0.0) }
    val shoved = shoveSortOrders(orders, target)
    val changes = buildList {
      shoved.updates.forEach { (other, order) -> add(sortOrder(ACCOUNTS, other.value, order)) }
      add(sortOrder(ACCOUNTS, id.value, shoved.sortOrder))
      if (group is To) add(localChange(ACCOUNTS, id.value, GROUP_ID, group.value?.value))
    }
    val previous = orders.toMap()
    val undo = buildList {
      shoved.updates.keys.forEach { other ->
        add(sortOrder(ACCOUNTS, other.value, previous.getValue(other)))
      }
      add(sortOrder(ACCOUNTS, id.value, account.sort_order ?: 0.0))
      if (group is To) {
        add(localChange(ACCOUNTS, id.value, GROUP_ID, account.account_group_id?.value))
      }
    }
    sync(changes)
    return AccountUndo(undo)
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

  // Returns what puts the old name back
  suspend fun renameGroup(id: AccountGroupId, name: String): AccountUndo {
    val group = groupDao.getAllActive().firstOrNull { it.id == id }
    checkNotNull(group) { "Account group $id not found" }
    checkGroupName(name, except = id)
    sync(localChange(ACCOUNT_GROUPS, id.value, "name", name))
    return AccountUndo(listOf(localChange(ACCOUNT_GROUPS, id.value, "name", group.name)))
  }

  /**
   * deleteAccountGroup(): its accounts become ungrouped. Returns what restores the group with its
   * accounts back in it.
   */
  suspend fun deleteGroup(id: AccountGroupId): AccountUndo {
    val accounts = accountDao.idsInGroup(id)
    val changes = buildList {
      val nullValue: String? = null
      accounts.forEach { add(localChange(ACCOUNTS, it.value, GROUP_ID, nullValue)) }
      add(tombstone(ACCOUNT_GROUPS, id.value))
    }
    sync(changes)
    val undo = buildList {
      add(untombstone(ACCOUNT_GROUPS, id.value))
      accounts.forEach { add(localChange(ACCOUNTS, it.value, GROUP_ID, id.value)) }
    }
    return AccountUndo(undo)
  }

  /**
   * moveAccountGroup(): puts [id] just before [target], or at the end without one. Returns what
   * puts it back.
   */
  suspend fun moveGroup(id: AccountGroupId, target: AccountGroupId?): AccountUndo {
    val groups = groupDao.getAllActive().map { it.id to (it.sort_order ?: 0.0) }
    val previous = groups.toMap()
    checkNotNull(previous[id]) { "Account group $id not found" }
    val shoved = shoveSortOrders(groups, target)
    val changes = buildList {
      shoved.updates.forEach { (other, order) ->
        add(sortOrder(ACCOUNT_GROUPS, other.value, order))
      }
      add(sortOrder(ACCOUNT_GROUPS, id.value, shoved.sortOrder))
    }
    val undo =
      (shoved.updates.keys + id).map { group ->
        sortOrder(ACCOUNT_GROUPS, group.value, previous.getValue(group))
      }
    sync(changes)
    return AccountUndo(undo)
  }

  // Writes back the values the undone change replaced, in one batch, and deletes what it added
  suspend fun undo(undo: AccountUndo) {
    if (undo.deleteTransactions.isEmpty()) {
      if (undo.changes.isNotEmpty()) sync(undo.changes)
      return
    }
    writer.write(runTransfers = true) {
      add(undo.changes)
      delete(undo.deleteTransactions)
    }
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

  private fun notesId(id: AccountId) = "account-${id.value}"

  private fun notesChange(id: AccountId, notes: String) =
    localChange(NOTES, notesId(id), "note", notes)

  private fun sortOrder(dataset: String, row: String, order: Double) =
    localChange(dataset, row, "sort_order", order)

  private companion object {
    const val GROUP_ID = "account_group_id"
    const val CLOSED = "closed"
    const val CLOSING_NOTE = "Closing account"
  }
}

// The values a change replaced and the transactions it added, for [AccountWriter.undo]
data class AccountUndo(
  val changes: List<LocalChange>,
  val deleteTransactions: Set<TransactionId> = emptySet(),
)

data class CloseOutcome(val result: CloseResult, val undo: AccountUndo)

enum class CloseResult {
  Closed,
  Deleted,
  Unchanged,
}

class GroupNameTakenException(val existing: String) :
  IllegalArgumentException("An '$existing' account group already exists")
