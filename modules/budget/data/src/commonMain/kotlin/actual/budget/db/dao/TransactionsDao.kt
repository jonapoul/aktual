package actual.budget.db.dao

import actual.budget.db.BudgetDatabase
import actual.budget.db.GetIds
import actual.budget.db.GetIdsByAccount
import actual.budget.db.transactions.GetById
import actual.budget.model.AccountId
import actual.budget.model.TransactionId
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

  fun observeIds(): Flow<List<GetIds>> = queries
    .getIds()
    .asFlow()
    .mapToList(contexts.default)
    .distinctUntilChanged()

  fun observeIdsByAccount(account: AccountId): Flow<List<GetIdsByAccount>> = queries
    .getIdsByAccount(account)
    .asFlow()
    .mapToList(contexts.default)
    .distinctUntilChanged()
}
