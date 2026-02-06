package aktual.budget.db.dao

import aktual.budget.db.Accounts
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.withResult
import aktual.budget.model.AccountId
import alakazam.kotlin.CoroutineContexts
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow

class AccountsDao(database: BudgetDatabase, private val contexts: CoroutineContexts) {
  private val queries = database.accountsQueries

  fun observe(id: AccountId): Flow<Accounts?> = queries
    .getById(id)
    .asFlow()
    .mapToOneOrNull(contexts.default)

  suspend operator fun get(id: AccountId): Accounts? = queries.withResult { getById(id).executeAsOneOrNull() }
}
