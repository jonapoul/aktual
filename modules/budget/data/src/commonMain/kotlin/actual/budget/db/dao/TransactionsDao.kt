package actual.budget.db.dao

import actual.budget.db.BudgetDatabase
import actual.budget.db.GetIds
import actual.budget.db.GetIdsByAccount
import actual.budget.db.V_transactions
import actual.budget.db.asListFlow
import actual.budget.db.asSingleNullableFlow
import actual.budget.db.withResult
import actual.budget.model.AccountId
import actual.budget.model.CategoryId
import actual.budget.model.PayeeId
import actual.budget.model.TransactionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class TransactionsDao(database: BudgetDatabase) {
  private val queries = database.transactionsQueries

  fun observeById(id: TransactionId): Flow<V_transactions?> = queries
    .getById(id)
    .asSingleNullableFlow()
    .distinctUntilChanged()

  fun observeIds(): Flow<List<GetIds>> = queries
    .getIds()
    .asListFlow()
    .distinctUntilChanged()

  fun observeIdsByAccount(account: AccountId): Flow<List<GetIdsByAccount>> = queries
    .getIdsByAccount(account)
    .asListFlow()
    .distinctUntilChanged()

  suspend fun getNames(account: AccountId, payee: PayeeId, category: CategoryId) =
    queries.withResult { getNames(account, payee, category).executeAsList() }
}
