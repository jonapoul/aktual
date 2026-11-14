package aktual.budget.transactions.domain

/**
 * Writes transactions, payees and accounts, like the db.insertTransaction() / updateTransaction() /
 * deleteTransaction() / insertPayee() / update('accounts') calls that
 * packages/loot-core/src/server/transactions/index.ts batchUpdateTransactions() and the bank sync
 * in accounts/sync.ts make inside batchMessages(). Everything goes through BudgetSyncController,
 * which also applies the changes to the local database.
 *
 * Rules aren't run, and transfers and category learning only when asked for.
 */
interface TransactionWriter {
  /**
   * Runs [block] to collect changes, then sends them all in one syncChanges() call, unless there
   * are none. Returns whatever [block] returns, e.g. the IDs it created. Calls run one at a time,
   * so each batch sees what the last one wrote. Don't call it again from inside [block].
   *
   * [runTransfers] is batchUpdateTransactions()'s flag of that name: once the changes are written,
   * the other side of every transfer among them is added, updated or removed to match, in a second
   * syncChanges() call. Bank sync leaves it off, as upstream's does.
   *
   * [learnCategories] is the flag of that name too: afterwards, [CategoryLearner] looks at the
   * transactions that were added or given a category. Only user edits turn it on.
   */
  suspend fun <R> write(
    runTransfers: Boolean = false,
    learnCategories: Boolean = false,
    block: suspend TransactionBatch.() -> R,
  ): R
}
