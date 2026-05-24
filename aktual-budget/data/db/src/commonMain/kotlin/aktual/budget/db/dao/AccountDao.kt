package aktual.budget.db.dao

import aktual.budget.db.Accounts
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.accounts.GetAllActive
import aktual.budget.db.withResult
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.zacsweers.metro.Inject
import kotlin.uuid.Uuid

@Inject
class AccountDao(database: BudgetDatabase) {
  private val queries = database.accountsQueries

  suspend fun insert(
    id: AccountId,
    accountId: String? = null,
    name: String? = null,
    officialName: String? = null,
    bank: Uuid? = null,
    offBudget: Boolean = false,
    accountSyncSource: AccountSyncSource? = null,
  ) = queries.withoutResult {
    insert(
      id = id,
      account_id = accountId,
      name = name,
      official_name = officialName,
      bank = bank,
      offbudget = offBudget,
      account_sync_source = accountSyncSource,
    )
  }

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

  // All non-tombstoned accounts (including closed) keyed by ID — schedules may reference closed
  // accounts
  suspend fun nameMap(): Map<AccountId, String?> = queries.withResult {
    getAllNames().awaitAsList().associate { it.id to it.name }
  }
}
