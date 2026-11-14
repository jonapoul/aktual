package aktual.budget.transactions.domain

import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.LocalChange
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import kotlinx.datetime.LocalDate

/**
 * Collects the changes of one [TransactionWriter.write] call, so they all go out in a single
 * BudgetSyncController.syncChanges(). Nothing touches the database until the block returns, so
 * anything added earlier in the same batch (payees, split children) is tracked here.
 */
@Suppress("ComplexInterface")
interface TransactionBatch {
  /**
   * db.insertTransaction(), plus batchUpdateTransactions() clearing the category of split parents
   * and off-budget transactions. Null fields are skipped, as convertForInsert() does.
   */
  suspend fun insert(transaction: NewTransaction): TransactionId

  /**
   * db.updateTransaction(), plus batchUpdateTransactions() clearing the category when the
   * transaction moves to an off-budget account or is made a split parent. Upstream's callers pass
   * the whole transaction, so setting a category here is also checked against the account and
   * isParent flag the transaction already has.
   */
  suspend fun update(update: TransactionUpdate)

  // The parent's update goes first, so a part added to a new split finds it already a parent
  suspend fun write(changes: TransactionChanges)

  suspend fun delete(id: TransactionId)

  /**
   * db.deleteTransaction() for each transaction and, as batchUpdateTransactions() does, every child
   * of a split parent among them.
   */
  suspend fun delete(ids: Collection<TransactionId>)

  /**
   * db.insertPayee(): the payee plus a payee_mapping row pointing at itself. [id] is for payees
   * that were given one before being saved, like those rules create.
   */
  fun insertPayee(
    name: String,
    transferAccount: AccountId? = null,
    id: PayeeId? = null,
  ): PayeeId

  /**
   * accounts/payees.ts createPayee(): the ID of the live payee with this name, ignoring case, or of
   * a new one if there's none.
   */
  suspend fun createPayee(name: String): PayeeId

  /** accounts/payees.ts getStartingBalancePayee() */
  suspend fun startingBalancePayee(): StartingBalancePayee

  /**
   * The starting balance transaction that accounts/app.ts createAccount() and accounts/sync.ts
   * processBankSyncDownload() add. Off-budget accounts get no category.
   */
  suspend fun insertStartingBalance(
    account: AccountId,
    amount: Amount,
    date: LocalDate,
  ): TransactionId

  /**
   * The forced branch of accounts/app.ts closeAccount(): deletes the account, its transactions and
   * its transfer payee. The other side of each transfer stays, as an ordinary transaction.
   */
  suspend fun deleteAccount(id: AccountId)

  /** accounts/app.ts updateAccount() with a new name. */
  fun renameAccount(id: AccountId, name: String)

  /**
   * db.insertAccount(): an open account at the end of the on or [offBudget] list. Its transfer
   * payee isn't added, see [createAccount].
   */
  suspend fun insertAccount(name: String, offBudget: Boolean): AccountId

  /**
   * accounts/app.ts createAccount(): the account in [group], its transfer payee and, unless
   * [balance] is zero, a starting balance on [date].
   */
  suspend fun createAccount(
    name: String,
    offBudget: Boolean,
    balance: Amount,
    date: LocalDate,
    group: AccountGroupId? = null,
  ): AccountId

  // Changes built elsewhere, like a bank link, to go out with the rest of the batch
  fun add(changes: Collection<LocalChange>)

  /** db.update('accounts', ...) with any of the bank sync or reconciliation fields. */
  fun updateAccount(update: AccountUpdate)
}
