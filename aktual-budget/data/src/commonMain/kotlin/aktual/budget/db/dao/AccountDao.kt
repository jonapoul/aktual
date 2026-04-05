package aktual.budget.db.dao

import aktual.budget.db.Accounts
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.withResult
import aktual.budget.model.AccountId

class AccountDao(database: BudgetDatabase) {
  private val queries = database.accountsQueries

  suspend operator fun get(id: AccountId): Accounts? = queries.withResult {
    getById(id).executeAsOneOrNull()
  }

  suspend fun name(id: AccountId): String = queries.withResult {
    getName(id).executeAsOneOrNull()?.name ?: error("Required name for $id")
  }

  suspend fun names(ids: List<AccountId>): List<String> = queries.withResult {
    getNames(ids).executeAsList().map { a -> a.name ?: error("Required name for $a") }
  }
}
