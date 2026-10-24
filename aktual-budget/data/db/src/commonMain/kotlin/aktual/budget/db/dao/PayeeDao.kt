package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.GetAllWithTransfer
import aktual.budget.db.Payees
import aktual.budget.db.payees.GetAllActive
import aktual.budget.db.withResult
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.PayeeId
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate

@Inject
class PayeeDao(database: BudgetDatabase) {
  private val queries = database.payeesQueries
  private val mappings = database.payeeMappingQueries

  suspend fun insert(id: PayeeId, name: String?, transferAccount: AccountId? = null) =
    queries.withoutResult {
      insert(
        id = id,
        name = name,
        category = null,
        tombstone = false,
        transfer_acct = transferAccount,
        favorite = false,
        learn_categories = null,
      )
      mappings.insert(id = id, targetId = id)
    }

  suspend operator fun get(id: PayeeId): Payees? = queries.withResult {
    getById(id).awaitAsOneOrNull()
  }

  // Every payee that isn't deleted, transfer payees included
  suspend fun aliveNames(): Map<PayeeId, String?> = queries.withResult {
    getAliveIdsAndNames().awaitAsList().associate { it.id to it.name }
  }

  suspend fun name(id: PayeeId): String? = queries.withResult {
    getName(id).awaitAsOneOrNull()?.name
  }

  suspend fun names(ids: List<PayeeId>): List<String> = queries.withResult {
    getNames(ids).awaitAsList().map { p -> p.name ?: error("Required name for $p") }
  }

  suspend fun getAllActive(): List<GetAllActive> = queries.withResult {
    getAllActive().awaitAsList()
  }

  suspend fun getAllWithTransfer(): List<GetAllWithTransfer> = queries.withResult {
    getAllWithTransfer().awaitAsList()
  }

  // As upstream's getCommonPayees, the IDs of the payees used most since this date, most used first
  suspend fun common(since: LocalDate, limit: Long): List<PayeeId> = queries.withResult {
    getCommon(since, limit).awaitAsList().map { it.id }
  }

  // The account this payee transfers to, null if it's an ordinary payee
  suspend fun transferAccount(id: PayeeId): AccountId? = queries.withResult {
    getTransferAccount(id).awaitAsOneOrNull()?.transfer_acct
  }

  // The payee that stands in for this account on the other side of a transfer
  suspend fun transferPayee(account: AccountId): PayeeId? = queries.withResult {
    getTransferPayee(account).awaitAsOneOrNull()
  }
}
