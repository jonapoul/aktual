package aktual.budget.transactions.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.BankSyncStatus
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.ScheduleId
import aktual.budget.model.TransactionId
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject

/**
 * A transaction to insert, using the field names of upstream's TransactionEntity rather than the
 * columns of the transactions table. Defaults match the transactions schema in
 * packages/loot-core/src/server/aql/schema/index.ts.
 *
 * A split is a parent with [isParent] set, plus one child per line with [parentId] pointing at it.
 * Children carry the parent's [account] and [date].
 */
data class NewTransaction(
  val account: AccountId,
  val date: LocalDate,
  val amount: Amount = Zero,
  val payee: PayeeId? = null,
  val category: CategoryId? = null,
  val notes: String? = null,
  val importedId: String? = null,
  val importedPayee: String? = null,
  val cleared: Boolean = true,
  val reconciled: Boolean = false,
  val isParent: Boolean = false,
  val parentId: TransactionId? = null,
  val startingBalance: Boolean = false,
  val transferId: TransactionId? = null,
  val schedule: ScheduleId? = null,
  val error: JsonObject? = null,
  val rawSyncedData: String? = null,
  val sortOrder: Long? = null,
  val id: TransactionId? = null,
)

/**
 * A partial update of one transaction. Only the fields that are set produce changes: null leaves a
 * non-nullable column alone, and [Patch.Keep] does the same for a nullable one. A null [isChild]
 * follows [parentId] when that's set.
 */
data class TransactionUpdate(
  val id: TransactionId,
  val account: AccountId? = null,
  val date: LocalDate? = null,
  val amount: Amount? = null,
  val cleared: Boolean? = null,
  val reconciled: Boolean? = null,
  val isParent: Boolean? = null,
  val isChild: Boolean? = null,
  val startingBalance: Boolean? = null,
  val sortOrder: Long? = null,
  val payee: Patch<PayeeId?> = Keep,
  val category: Patch<CategoryId?> = Keep,
  val notes: Patch<String?> = Keep,
  val importedId: Patch<String?> = Keep,
  val importedPayee: Patch<String?> = Keep,
  val parentId: Patch<TransactionId?> = Keep,
  val transferId: Patch<TransactionId?> = Keep,
  val schedule: Patch<ScheduleId?> = Keep,
  val error: Patch<JsonObject?> = Keep,
  val rawSyncedData: Patch<String?> = Keep,
)

/** The bank sync fields of an account, written like upstream's db.update('accounts', ...). */
data class AccountUpdate(
  val id: AccountId,
  val balanceCurrent: Patch<Amount?> = Keep,
  val lastSync: Patch<Instant?> = Keep,
  val bankSyncStatus: Patch<BankSyncStatus?> = Keep,
)

/** packages/loot-core/src/server/accounts/payees.ts getStartingBalancePayee() */
data class StartingBalancePayee(val id: PayeeId, val category: CategoryId?)
