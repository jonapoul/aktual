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
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.zacsweers.metro.Inject
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate

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

data class BankSyncAccount(
  val id: AccountId,
  val name: String?,
  val accountId: String,
  val source: AccountSyncSource?,
  val bankId: BankId,
  val bankName: String?,
)

@Inject
class BankSyncDao(database: BudgetDatabase) {
  private val queries = database.bankSyncQueries
  private val banks = database.banksQueries

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

  suspend fun childIds(parent: TransactionId): List<TransactionId> = queries.withResult {
    bankSyncChildIds(parent).awaitAsList()
  }

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

  suspend fun oldestDate(account: AccountId, today: LocalDate): LocalDate? = queries.withResult {
    bankSyncOldestDate(account, today).awaitAsOneOrNull()
  }

  suspend fun exampleData(account: AccountId, deposit: Boolean): String? = queries.withResult {
    val query = if (deposit) bankSyncDepositExample(account) else bankSyncPaymentExample(account)
    query.awaitAsOneOrNull()
  }

  suspend fun findBank(bankId: BankId?, name: String?): Uuid? = queries.withResult {
    bankSyncFindBank(bankId, name).awaitAsOneOrNull()
  }

  suspend fun bankId(bank: Uuid): BankId? = banks.withResult {
    getBankId(bank).awaitAsOneOrNull()?.bank_id
  }

  suspend fun bankUsers(bank: Uuid): Long = queries.withResult {
    bankSyncBankUsers(bank).awaitAsOne()
  }

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
