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
import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.budget.model.localChange
import aktual.budget.model.messageValue
import aktual.budget.model.sortOrderAfter
import aktual.budget.model.tombstone
import aktual.core.utils.UuidGenerator
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject

/**
 * Column names are those of the transactions table, mapped from the TransactionEntity fields the
 * way schemaConfig.views.transactions.fields in packages/loot-core/src/server/aql/schema/index.ts
 * does. As upstream, a new row's id is the message row, so there's no message for the id column
 * itself.
 */
internal class TransactionBatchImpl(
  private val accountDao: AccountDao,
  private val categoryDao: CategoryDao,
  private val payeeDao: PayeeDao,
  private val transactionDao: TransactionDao,
  private val uuidGenerator: UuidGenerator,
  private val clock: Clock,
) : TransactionBatch {
  private val changes = mutableListOf<LocalChange>()
  private val offBudget = mutableMapOf<AccountId, Boolean>()
  // The parent each transaction was given in this batch, null if it was detached
  private val batchParents = mutableMapOf<TransactionId, TransactionId?>()
  // The account and isParent flag each transaction was given in this batch
  private val batchAccounts = mutableMapOf<TransactionId, AccountId>()
  private val batchIsParent = mutableMapOf<TransactionId, Boolean>()
  private val added = mutableSetOf<TransactionId>()
  private val updated = mutableSetOf<TransactionId>()
  private val deleted = mutableSetOf<TransactionId>()
  // Added or updated with a category, for category learning
  private val categorised = mutableSetOf<TransactionId>()
  private val newPayees = mutableMapOf<String, PayeeId>()
  private var existingPayees: Map<String, PayeeId>? = null

  internal fun changes(): List<LocalChange> = changes.toList()

  // Each transaction is in one set only: deleted wins, then added
  internal fun touched(): TouchedTransactions =
    TouchedTransactions(
      added = added - deleted,
      updated = updated - added - deleted,
      deleted = deleted.toSet(),
    )

  internal fun categorised(): Set<TransactionId> = categorised - deleted

  override suspend fun insert(transaction: NewTransaction): TransactionId {
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
        SORT_ORDER to MessageValue.Number(t.sortOrder ?: clock.now().toEpochMilliseconds()),
        "cleared" to t.cleared.messageValue(),
        "reconciled" to t.reconciled.messageValue(),
        "schedule" to t.schedule?.value.orSkip(),
        "raw_synced_data" to t.rawSyncedData.orSkip(),
        "isParent" to t.isParent.trueOrSkip(),
        "isChild" to (t.parentId != null).trueOrSkip(),
        "parent_id" to t.parentId?.value.orSkip(),
      )
    addAll(TRANSACTIONS, id.value, columns)
    added += id
    if (category != null) categorised += id
    if (t.parentId != null) batchParents[id] = t.parentId
    batchAccounts[id] = t.account
    batchIsParent[id] = t.isParent
    return id
  }

  override suspend fun update(update: TransactionUpdate) {
    val u = update
    u.date?.let(::checkDate)
    val category = if (clearsCategory(u)) Patch.To(null) else u.category
    // As insert() does, isChild follows parentId unless the caller sets it
    val isChild = u.isChild ?: (u.parentId as? To)?.let { it.value != null }
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
        SORT_ORDER to u.sortOrder?.let(MessageValue::Number),
        "cleared" to u.cleared?.messageValue(),
        "reconciled" to u.reconciled?.messageValue(),
        "schedule" to u.schedule.messageValue { it.value.messageValue() },
        "raw_synced_data" to u.rawSyncedData.messageValue { it.messageValue() },
        "isParent" to u.isParent?.messageValue(),
        "isChild" to isChild?.messageValue(),
        "parent_id" to u.parentId.messageValue { it.value.messageValue() },
      )
    addAll(TRANSACTIONS, u.id.value, columns)
    updated += u.id
    if ((category as? To)?.value != null) categorised += u.id
    (u.parentId as? To)?.let { batchParents[u.id] = it.value }
    u.account?.let { batchAccounts[u.id] = it }
    u.isParent?.let { batchIsParent[u.id] = it }
  }

  override suspend fun write(changes: TransactionChanges) {
    changes.updated.forEach { update(it) }
    changes.added.forEach { insert(it) }
    if (changes.deleted.isNotEmpty()) delete(changes.deleted)
  }

  override suspend fun delete(id: TransactionId) = delete(listOf(id))

  override suspend fun delete(ids: Collection<TransactionId>) {
    val parents = ids.toSet()
    val withChildren = LinkedHashSet(ids)
    // Stored children, unless this batch already moved them elsewhere
    withChildren += transactionDao.childIds(ids).filterNot { it in batchParents }
    withChildren += batchParents.filterValues { it != null && it in parents }.keys
    for (id in withChildren) {
      if (deleted.add(id)) changes += tombstone(TRANSACTIONS, id.value)
    }
  }

  override fun insertPayee(
    name: String,
    transferAccount: AccountId?,
    id: PayeeId?,
  ): PayeeId {
    val idOrNew = id ?: uuidGenerator(::PayeeId)
    val row = idOrNew.value
    changes += localChange(PAYEES, row, "name", name)
    if (transferAccount != null) {
      changes += localChange(PAYEES, row, "transfer_acct", transferAccount.value)
    }
    changes += localChange(PAYEE_MAPPING, row, "targetId", row)
    newPayees.getOrPut(name.lowercase()) { idOrNew }
    return idOrNew
  }

  override suspend fun createPayee(name: String): PayeeId {
    val key = name.lowercase()
    return existingPayees()[key] ?: newPayees[key] ?: insertPayee(name)
  }

  override suspend fun startingBalancePayee(): StartingBalancePayee {
    val category = categoryDao.startingBalanceCategory()
    return StartingBalancePayee(id = createPayee(STARTING_BALANCE), category = category)
  }

  override suspend fun insertStartingBalance(
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

  override suspend fun deleteAccount(id: AccountId) {
    val transactions = transactionDao.accountTransfers(id)
    for (linked in transactions.values) {
      if (linked != null && linked !in transactions) {
        update(TransactionUpdate(id = linked, payee = Patch.To(null), transferId = Patch.To(null)))
      }
    }
    delete(transactions.keys)
    payeeDao.transferPayee(id)?.let { changes += tombstone(PAYEES, it.value) }
    changes += tombstone(ACCOUNTS, id.value)
  }

  override fun renameAccount(id: AccountId, name: String) {
    changes += localChange(ACCOUNTS, id.value, "name", name)
  }

  override suspend fun insertAccount(name: String, offBudget: Boolean): AccountId {
    val id = uuidGenerator(::AccountId)
    val row = id.value
    val sortOrder = sortOrderAfter(accountDao.maxSortOrder(offBudget))
    changes += localChange(ACCOUNTS, row, "name", name)
    changes += localChange(ACCOUNTS, row, "offbudget", offBudget)
    changes += localChange(ACCOUNTS, row, "closed", false)
    changes += localChange(ACCOUNTS, row, SORT_ORDER, sortOrder)
    this.offBudget[id] = offBudget
    return id
  }

  override suspend fun createAccount(
    name: String,
    offBudget: Boolean,
    balance: Amount,
    date: LocalDate,
    group: AccountGroupId?,
  ): AccountId {
    val id = insertAccount(name, offBudget)
    if (group != null) changes += localChange(ACCOUNTS, id.value, "account_group_id", group.value)
    insertPayee(name = "", transferAccount = id)
    if (balance != Zero) insertStartingBalance(id, balance, date)
    return id
  }

  override fun add(changes: Collection<LocalChange>) {
    this.changes += changes
  }

  override fun updateAccount(update: AccountUpdate) {
    val columns =
      listOf(
        "balance_current" to update.balanceCurrent.messageValue { it.messageValue() },
        // Upstream stores the milliseconds as a string, e.g. new Date().getTime().toString()
        "last_sync" to
          update.lastSync.messageValue { it.toEpochMilliseconds().toString().messageValue() },
        "bank_sync_status" to update.bankSyncStatus.messageValue { it.value.messageValue() },
        "last_reconciled" to
          update.lastReconciled.messageValue {
            it.toEpochMilliseconds().toString().messageValue()
          },
      )
    addAll(ACCOUNTS, update.id.value, columns)
  }

  private fun addAll(dataset: String, row: String, columns: List<Pair<String, MessageValue?>>) {
    for ((column, value) in columns) {
      if (value != null) changes += LocalChange(dataset, row, column, value)
    }
  }

  private suspend fun clearsCategory(u: TransactionUpdate): Boolean {
    val newAccount = u.account
    if (u.isParent == true || newAccount != null && isOffBudget(newAccount)) return true
    if ((u.category as? To)?.value == null) return false
    val stored = transactionDao.row(u.id)
    val isParent = u.isParent ?: batchIsParent[u.id] ?: stored?.isParent
    val account = u.account ?: batchAccounts[u.id] ?: stored?.acct
    val isOffBudget = account != null && isOffBudget(account)
    return isParent == true || isOffBudget
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

// The transactions a batch wrote, whose transfers need looking at
internal data class TouchedTransactions(
  val added: Set<TransactionId>,
  val updated: Set<TransactionId>,
  val deleted: Set<TransactionId>,
)

// convertInputType() rejects dates before this
val MIN_TRANSACTION_DATE = LocalDate(year = 1995, month = 1, day = 1)

private const val STARTING_BALANCE = "Starting Balance"

private const val SORT_ORDER = "sort_order"

private fun checkDate(date: LocalDate) =
  require(date >= MIN_TRANSACTION_DATE) { "Invalid date: $date" }

private fun String?.orSkip(): MessageValue? = this?.let(MessageValue::String)

private fun Boolean.trueOrSkip(): MessageValue? = if (this) MessageValue.Number(1) else null

private fun Amount.messageValue(): MessageValue = MessageValue.Number(toLong())

private fun JsonObject.encode(): String = DbJson.encodeToString(JsonObject.serializer(), this)

// Null means there's no change to send
private fun <T : Any> Patch<T?>.messageValue(encode: (T) -> MessageValue): MessageValue? =
  when (this) {
    Keep -> null
    is To -> value?.let(encode) ?: Null
  }
