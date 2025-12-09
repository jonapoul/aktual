package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.transactions.GetById
import aktual.budget.model.AccountId
import aktual.budget.model.TransactionId
import alakazam.kotlin.core.CoroutineContexts
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class TransactionsDao(
  database: BudgetDatabase,
  private val contexts: CoroutineContexts,
) {
  private val queries = database.transactionsQueries

  fun observeById(id: TransactionId): Flow<GetById?> = queries
    .getById(id)
    .asFlow()
    .mapToOneOrNull(contexts.default)
    .distinctUntilChanged()

  fun observeAllIds(): Flow<List<TransactionId>> = queries
    .getIds()
    .asFlow()
    .mapToList(contexts.default)
    .distinctUntilChanged()

  fun observeIdsByAccount(account: AccountId): Flow<List<TransactionId>> = queries
    .getIdsByAccount(account)
    .asFlow()
    .mapToList(contexts.default)
    .distinctUntilChanged()
}
