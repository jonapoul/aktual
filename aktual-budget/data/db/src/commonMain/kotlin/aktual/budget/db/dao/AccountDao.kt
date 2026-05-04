package aktual.budget.db.dao

import aktual.budget.db.Accounts
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.accounts.GetAllActive
import aktual.budget.db.withResult
import aktual.budget.model.AccountId
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull

class AccountDao(database: BudgetDatabase) {
  private val queries = database.accountsQueries

  suspend operator fun get(id: AccountId): Accounts? = queries.withResult {
    getById(id).awaitAsOneOrNull()
  }

  suspend fun name(id: AccountId): String = queries.withResult {
    getName(id).awaitAsOneOrNull()?.name ?: error("Required name for $id")
  }

  suspend fun names(ids: List<AccountId>): List<String> = queries.withResult {
    getNames(ids).awaitAsList().map { a -> a.name ?: error("Required name for $a") }
  }

  suspend fun getAllActive(): List<GetAllActive> = queries.withResult {
    getAllActive().awaitAsList()
  }
}
