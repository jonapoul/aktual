package aktual.budget.transactions.domain

import aktual.budget.db.DbJson
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.DatabaseTables.PAYEES
import aktual.budget.db.dao.DatabaseTables.PAYEE_MAPPING
import aktual.budget.db.dao.DatabaseTables.TRANSACTIONS
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.budget.model.messageValue
import aktual.budget.model.tombstone
import aktual.core.UuidGenerator
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.serialization.json.JsonObject

/**
 * Collects the changes of one [TransactionWriter.write] call, so they all go out in a single
 * BudgetSyncController.syncChanges(). Nothing touches the database until the block returns, so
 * anything added earlier in the same batch (payees, split children) is tracked here.
 *
 * Column names are those of the transactions table, mapped from the TransactionEntity fields the
 * way schemaConfig.views.transactions.fields in packages/loot-core/src/server/aql/schema/index.ts
 * does. As upstream, a new row's id is the message row, so there's no message for the id column
 * itself.
 */
class TransactionBatch
internal constructor(
  private val accountDao: AccountDao,
  private val categoryDao: CategoryDao,
  private val payeeDao: PayeeDao,
  private val transactionDao: TransactionDao,
  private val uuidGenerator: UuidGenerator,
  private val clock: Clock,
) {
  private val changes = mutableListOf<LocalChange>()
  private val offBudget = mutableMapOf<AccountId, Boolean>()
  private val newChildren = mutableMapOf<TransactionId, MutableList<TransactionId>>()
  private val deleted = mutableSetOf<TransactionId>()
  private val newPayees = mutableMapOf<String, PayeeId>()
  private var existingPayees: Map<String, PayeeId>? = null

  internal fun changes(): List<LocalChange> = changes.toList()

  /**
   * db.insertTransaction(), plus batchUpdateTransactions() clearing the category of split parents
   * and off-budget transactions. Null fields are skipped, as convertForInsert() does.
   */
  suspend fun insert(transaction: NewTransaction): TransactionId {
    val t = transaction
    checkDate(t.date)
    val id = t.id ?: uuidGenerator(::TransactionId)
    val category = if (t.isParent || isOffBudget(t.account)) null else t.category
    val columns =
      listOf(
        "acct" to t.account.value.messageValue(),
        "category" to category?.value.orSkip(),
        "amount" to t.amount.messageValue(),
        "description" to t.payee?.value.orSkip(),
        "notes" to t.notes.orSkip(),
        "date" to t.date.messageValue(),
        "financial_id" to t.importedId.orSkip(),
        "error" to t.error?.encode().orSkip(),
        "imported_description" to t.importedPayee.orSkip(),
        "starting_balance_flag" to t.startingBalance.trueOrSkip(),
        "transferred_id" to t.transferId?.value.orSkip(),
        "sort_order" to MessageValue.Number(t.sortOrder ?: clock.now().toEpochMilliseconds()),
        "cleared" to t.cleared.messageValue(),
        "reconciled" to t.reconciled.messageValue(),
        "schedule" to t.schedule?.value.orSkip(),
        "raw_synced_data" to t.rawSyncedData.orSkip(),
        "isParent" to t.isParent.trueOrSkip(),
        "isChild" to (t.parentId != null).trueOrSkip(),
        "parent_id" to t.parentId?.value.orSkip(),
      )
    addAll(TRANSACTIONS, id.value, columns)
    if (t.parentId != null) newChildren.getOrPut(t.parentId) { mutableListOf() } += id
    return id
  }

  /**
   * db.updateTransaction(), plus batchUpdateTransactions() clearing the category when the
   * transaction moves to an off-budget account or is made a split parent.
   */
  suspend fun update(update: TransactionUpdate) {
    val u = update
    u.date?.let(::checkDate)
    val clearCategory = u.isParent == true || u.account != null && isOffBudget(u.account)
    val category = if (clearCategory) Patch.To(null) else u.category
    // As insert() does, isChild follows parentId unless the caller sets it
    val isChild = u.isChild ?: (u.parentId as? Patch.To)?.let { it.value != null }
    val columns =
      listOf(
        "acct" to u.account?.value?.messageValue(),
        "category" to category.messageValue { it.value.messageValue() },
        "amount" to u.amount?.messageValue(),
        "description" to u.payee.messageValue { it.value.messageValue() },
        "notes" to u.notes.messageValue { it.messageValue() },
        "date" to u.date?.messageValue(),
        "financial_id" to u.importedId.messageValue { it.messageValue() },
        "error" to u.error.messageValue { it.encode().messageValue() },
        "imported_description" to u.importedPayee.messageValue { it.messageValue() },
        "starting_balance_flag" to u.startingBalance?.messageValue(),
        "transferred_id" to u.transferId.messageValue { it.value.messageValue() },
        "sort_order" to u.sortOrder?.let(MessageValue::Number),
        "cleared" to u.cleared?.messageValue(),
        "reconciled" to u.reconciled?.messageValue(),
        "schedule" to u.schedule.messageValue { it.value.messageValue() },
        "raw_synced_data" to u.rawSyncedData.messageValue { it.messageValue() },
        "isParent" to u.isParent?.messageValue(),
        "isChild" to isChild?.messageValue(),
        "parent_id" to u.parentId.messageValue { it.value.messageValue() },
      )
    addAll(TRANSACTIONS, u.id.value, columns)
    val parent = (u.parentId as? Patch.To)?.value
    if (parent != null) newChildren.getOrPut(parent) { mutableListOf() } += u.id
  }

  suspend fun delete(id: TransactionId) = delete(listOf(id))

  /**
   * db.deleteTransaction() for each transaction and, as batchUpdateTransactions() does, every child
   * of a split parent among them.
   */
  suspend fun delete(ids: Collection<TransactionId>) {
    val withChildren = LinkedHashSet(ids)
    withChildren += transactionDao.childIds(ids)
    ids.forEach { id -> withChildren += newChildren[id].orEmpty() }
    for (id in withChildren) {
      if (deleted.add(id)) changes += tombstone(TRANSACTIONS, id.value)
    }
  }

  /** db.insertPayee(): the payee plus a payee_mapping row pointing at itself. */
  fun insertPayee(name: String, transferAccount: AccountId? = null): PayeeId {
    val id = uuidGenerator(::PayeeId)
    val row = id.value
    changes += LocalChange(PAYEES, row, "name", name.messageValue())
    if (transferAccount != null) {
      changes += LocalChange(PAYEES, row, "transfer_acct", transferAccount.value.messageValue())
    }
    changes += LocalChange(PAYEE_MAPPING, row, "targetId", row.messageValue())
    newPayees.getOrPut(name.lowercase()) { id }
    return id
  }

  /**
   * accounts/payees.ts createPayee(): the ID of the live payee with this name, ignoring case, or of
   * a new one if there's none.
   */
  suspend fun createPayee(name: String): PayeeId {
    val key = name.lowercase()
    return existingPayees()[key] ?: newPayees[key] ?: insertPayee(name)
  }

  /** accounts/payees.ts getStartingBalancePayee() */
  suspend fun startingBalancePayee(): StartingBalancePayee {
    val category = categoryDao.startingBalanceCategory()
    return StartingBalancePayee(id = createPayee(STARTING_BALANCE), category = category)
  }

  /**
   * The starting balance transaction that accounts/app.ts createAccount() and accounts/sync.ts
   * processBankSyncDownload() add. Off-budget accounts get no category.
   */
  suspend fun insertStartingBalance(
    account: AccountId,
    amount: Amount,
    date: LocalDate,
  ): TransactionId {
    val payee = startingBalancePayee()
    val transaction =
      NewTransaction(
        account = account,
        date = date,
        amount = amount,
        payee = payee.id,
        category = payee.category,
        cleared = true,
        startingBalance = true,
      )
    return insert(transaction)
  }

  /** db.update('accounts', ...) with any of the bank sync fields. */
  fun updateAccount(update: AccountUpdate) {
    val columns =
      listOf(
        "balance_current" to update.balanceCurrent.messageValue { it.messageValue() },
        // Upstream stores the milliseconds as a string, e.g. new Date().getTime().toString()
        "last_sync" to
          update.lastSync.messageValue { it.toEpochMilliseconds().toString().messageValue() },
        "bank_sync_status" to update.bankSyncStatus.messageValue { it.value.messageValue() },
      )
    addAll(ACCOUNTS, update.id.value, columns)
  }

  private fun addAll(dataset: String, row: String, columns: List<Pair<String, MessageValue?>>) {
    for ((column, value) in columns) {
      if (value != null) changes += LocalChange(dataset, row, column, value)
    }
  }

  private suspend fun isOffBudget(account: AccountId): Boolean =
    offBudget.getOrPut(account) {
      val row = accountDao[account]
      row != null && row.tombstone != true && row.offbudget == true
    }

  private suspend fun existingPayees(): Map<String, PayeeId> =
    existingPayees
      ?: buildMap {
        for ((id, name) in payeeDao.aliveNames()) {
          if (name != null) getOrPut(name.lowercase()) { id }
        }
      }
        .also { existingPayees = it }
}

// convertInputType() rejects dates before this
private val MIN_DATE = LocalDate(year = 1995, month = 1, day = 1)

private const val STARTING_BALANCE = "Starting Balance"

private fun checkDate(date: LocalDate) = require(date >= MIN_DATE) { "Invalid date: $date" }

private fun String?.orSkip(): MessageValue? = this?.let(MessageValue::String)

private fun Boolean.trueOrSkip(): MessageValue? = if (this) MessageValue.Number(1) else null

private fun Amount.messageValue(): MessageValue = MessageValue.Number(toLong())

private fun JsonObject.encode(): String = DbJson.encodeToString(JsonObject.serializer(), this)

// Dates are stored as integers like 20261001, see toDateRepr() in loot-core
@Suppress("MagicNumber")
private fun LocalDate.messageValue(): MessageValue =
  MessageValue.Number(year * 10_000L + month.number * 100L + day)

// Null means there's no change to send
private fun <T : Any> Patch<T?>.messageValue(encode: (T) -> MessageValue): MessageValue? =
  when (this) {
    Keep -> null
    is Patch.To -> value?.let(encode) ?: MessageValue.Null
  }
