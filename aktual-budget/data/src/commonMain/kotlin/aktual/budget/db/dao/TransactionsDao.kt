package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.transactions.GetById
import aktual.budget.model.AccountId
import aktual.budget.model.TransactionId
import alakazam.kotlin.CoroutineContexts
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class TransactionsDao(
    database: BudgetDatabase,
    private val contexts: CoroutineContexts,
) {
  private val queries = database.transactionsQueries

  fun observeById(id: TransactionId): Flow<GetById?> =
      queries.getById(id).asFlow().mapToOneOrNull(contexts.default).distinctUntilChanged()

  fun getIdsPaged(limit: Long, offset: Long): List<TransactionId> =
      queries.getIdsPaged(limit, offset).executeAsList()

  fun getIdsByAccountPaged(account: AccountId, limit: Long, offset: Long): List<TransactionId> =
      queries.getIdsByAccountPaged(account, limit, offset).executeAsList()

  fun observeCount(): Flow<Long> =
      queries.getIdsCount().asFlow().mapToOne(contexts.default).distinctUntilChanged()

  fun observeCountByAccount(account: AccountId): Flow<Long> =
      queries
          .getIdsByAccountCount(account)
          .asFlow()
          .mapToOne(contexts.default)
          .distinctUntilChanged()
}
