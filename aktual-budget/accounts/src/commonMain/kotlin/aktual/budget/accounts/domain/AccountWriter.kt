package aktual.budget.accounts.domain

import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.budget.model.CategoryId
import aktual.budget.model.LocalChange
import aktual.budget.model.TransactionId
import aktual.budget.transactions.domain.Patch
import aktual.budget.transactions.domain.Patch.Keep

/**
 * Account and account group edits, as the handlers in packages/loot-core/src/server/accounts/app.ts
 * and account-groups/app.ts. Bank sync unlinking is left to the caller, see AccountDeleter.
 */
@Suppress("ComplexInterface")
interface AccountWriter {
  suspend fun rename(id: AccountId, name: String)

  // Account notes live in the notes table under "account-<id>"
  suspend fun setNotes(id: AccountId, notes: String)

  suspend fun notes(id: AccountId): String

  /**
   * The edit sheet's save: updateAccount() with a new name, plus its notes and group, in one batch.
   * Returns what puts them back.
   */
  suspend fun edit(
    id: AccountId,
    name: Patch<String> = Keep,
    notes: Patch<String> = Keep,
    group: Patch<AccountGroupId?> = Keep,
  ): AccountUndo

  suspend fun setGroup(id: AccountId, group: AccountGroupId?)

  // Returns what closes it again
  suspend fun reopen(id: AccountId): AccountUndo

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
  ): CloseOutcome

  /**
   * moveAccount(): puts [id] just before [target], or at the end without one, among the open
   * accounts on the same side of the budget, or among closed accounts if it's closed. [group] moves
   * it into another group too, null being ungrouped. Returns what puts it back.
   */
  suspend fun move(
    id: AccountId,
    target: AccountId?,
    group: Patch<AccountGroupId?> = Keep,
  ): AccountUndo

  /** insertAccountGroup(): a group after the last one. Names are unique, ignoring case. */
  suspend fun createGroup(name: String): AccountGroupId

  // Returns what puts the old name back
  suspend fun renameGroup(id: AccountGroupId, name: String): AccountUndo

  /**
   * deleteAccountGroup(): its accounts become ungrouped. Returns what restores the group with its
   * accounts back in it.
   */
  suspend fun deleteGroup(id: AccountGroupId): AccountUndo

  /**
   * moveAccountGroup(): puts [id] just before [target], or at the end without one. Returns what
   * puts it back.
   */
  suspend fun moveGroup(id: AccountGroupId, target: AccountGroupId?): AccountUndo

  // Writes back the values the undone change replaced, in one batch, and deletes what it added
  suspend fun undo(undo: AccountUndo)
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
