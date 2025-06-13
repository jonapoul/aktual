package actual.budget.db.dao

import actual.budget.db.Accounts
import actual.budget.db.BudgetDatabase
import actual.budget.db.asSingleNullableFlow
import actual.budget.model.AccountId
import kotlinx.coroutines.flow.Flow

class AccountsDao(database: BudgetDatabase) {
  private val queries = database.accountsQueries

  fun observe(id: AccountId): Flow<Accounts?> = queries
    .getById(id)
    .asSingleNullableFlow()
}
