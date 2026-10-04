package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.withResult
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.BankId
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate

/**
 * An existing transaction that a downloaded one could match, with the v_transactions fields that
 * reconciling reads. Payee and category are resolved through their mappings.
 *
 * @property parentId Only set for a split child.
 * @property rawSyncedData Only read for a match on imported_id, as upstream.
 */
data class BankSyncCandidate(
  val id: TransactionId,
  val date: LocalDate,
  val isParent: Boolean = false,
  val parentId: TransactionId? = null,
  val payee: PayeeId? = null,
  val category: CategoryId? = null,
  val notes: String? = null,
  val importedId: String? = null,
  val importedPayee: String? = null,
  val cleared: Boolean = false,
  val reconciled: Boolean = false,
  val rawSyncedData: String? = null,
)

/**
 * An account that bank sync downloads transactions for.
 *
 * @property accountId The provider's ID for the account.
 * @property source Null if the account was linked without one, which can't be synced.
 * @property bankId GoCardless' requisition ID.
 * @property bankName Enable Banking's name for the bank.
 */
data class BankSyncAccount(
  val id: AccountId,
  val name: String?,
  val accountId: String,
  val source: AccountSyncSource?,
  val bankId: BankId,
  val bankName: String?,
)

/** The lookups of bank sync, mostly packages/loot-core/src/server/accounts/sync.ts. */
@Inject
class BankSyncDao(database: BudgetDatabase) {
  private val queries = database.bankSyncQueries

  /**
   * The transaction in [account] with this imported_id. Deleted ones only match when
   * [includeDeleted] is set, so that they aren't imported again.
   */
  suspend fun matchByImportedId(
    importedId: String,
    account: AccountId,
    includeDeleted: Boolean,
  ): BankSyncCandidate? = queries.withResult {
    if (includeDeleted) {
      bankSyncMatchAny(importedId, account, ::matchRow).awaitAsOneOrNull()
    } else {
      bankSyncMatchAlive(importedId, account, ::matchRow).awaitAsOneOrNull()
    }
  }

  /**
   * Live transactions in [account] with this amount, dated between [from] and [to] inclusive, in
   * v_transactions order. With [onlyWithoutImportedId], those that already have an imported_id are
   * left out.
   */
  suspend fun fuzzyCandidates(
    account: AccountId,
    amount: Long,
    from: LocalDate,
    to: LocalDate,
    onlyWithoutImportedId: Boolean,
  ): List<BankSyncCandidate> = queries.withResult {
    if (onlyWithoutImportedId) {
      bankSyncFuzzyCandidatesStrict(from, to, amount, account, ::fuzzyRow).awaitAsList()
    } else {
      bankSyncFuzzyCandidates(from, to, amount, account, ::fuzzyRow).awaitAsList()
    }
  }

  // The live children of a split parent
  suspend fun childIds(parent: TransactionId): List<TransactionId> = queries.withResult {
    bankSyncChildIds(parent).awaitAsList()
  }

  // Open, linked accounts, off budget ones last
  suspend fun accounts(): List<BankSyncAccount> = queries.withResult {
    bankSyncAccounts { id, name, accountId, source, bankId, bankName ->
      BankSyncAccount(
        id = id,
        name = name,
        accountId = accountId,
        source = source,
        bankId = bankId,
        bankName = bankName,
      )
    }
      .awaitAsList()
  }

  // The date of the account's oldest live transaction up to [today]
  suspend fun oldestDate(account: AccountId, today: LocalDate): LocalDate? = queries.withResult {
    bankSyncOldestDate(account, today).awaitAsOneOrNull()
  }

  // Every category that isn't deleted
  suspend fun categoryIds(): Set<CategoryId> = queries.withResult {
    bankSyncCategoryIds().awaitAsList().toSet()
  }
}

@Suppress("LongParameterList")
private fun matchRow(
  id: TransactionId,
  isParent: Boolean?,
  date: LocalDate,
  payee: PayeeId?,
  category: CategoryId?,
  notes: String?,
  importedId: String?,
  importedPayee: PayeeId?,
  cleared: Boolean?,
  reconciled: Boolean?,
  rawSyncedData: String?,
) =
  BankSyncCandidate(
    id = id,
    date = date,
    isParent = isParent == true,
    payee = payee,
    // As v_transactions_internal does
    category = category.takeUnless { isParent == true },
    notes = notes,
    importedId = importedId,
    importedPayee = importedPayee?.value,
    cleared = cleared == true,
    reconciled = reconciled == true,
    rawSyncedData = rawSyncedData,
  )

@Suppress("LongParameterList")
private fun fuzzyRow(
  id: TransactionId,
  isParent: Boolean?,
  isChild: Boolean?,
  parentId: TransactionId?,
  date: LocalDate,
  payee: PayeeId?,
  category: CategoryId?,
  notes: String?,
  importedId: String?,
  importedPayee: PayeeId?,
  cleared: Boolean?,
  reconciled: Boolean?,
) =
  BankSyncCandidate(
    id = id,
    date = date,
    isParent = isParent == true,
    parentId = parentId.takeIf { isChild == true },
    payee = payee,
    category = category,
    notes = notes,
    importedId = importedId,
    importedPayee = importedPayee?.value,
    cleared = cleared == true,
    reconciled = reconciled == true,
  )
