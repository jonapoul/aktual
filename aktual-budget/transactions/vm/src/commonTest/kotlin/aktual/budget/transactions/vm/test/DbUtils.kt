package aktual.budget.transactions.vm.test

import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.BudgetId
import aktual.budget.model.CategoryId
import aktual.budget.model.DbMetadata
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.budget.transactions.vm.Transaction
import aktual.core.model.Token
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.serialization.json.JsonObject

internal val DATE_1 = LocalDate(2025, Month.JUNE, 1)
internal val DATE_2 = LocalDate(2025, Month.JUNE, 2)
internal val DATE_3 = LocalDate(2025, Month.JUNE, 3)

internal val TOKEN = Token("abc-123")
internal val BUDGET_ID = BudgetId("xyz-789")
internal val METADATA = DbMetadata(cloudFileId = BUDGET_ID)

internal val LINKED = AccountId("linked")

internal suspend fun AccountDao.insertAccount(id: AccountId, name: String) {
  insert(id = id, accountId = id.toString(), name = name, officialName = name)
}

internal suspend fun PayeeDao.insertPayee(id: PayeeId, name: String) = insert(id, name)

internal suspend fun CategoryDao.insertCategory(id: CategoryId, name: String) = insert(id, name)

internal suspend fun TransactionDao.insertTransaction(
  id: String,
  account: String,
  category: String?,
  payee: String,
  notes: String? = null,
  date: LocalDate = DATE_1,
  amount: Double = 123.45,
  isParent: Boolean = false,
  parent: String? = null,
  error: JsonObject? = null,
) =
  insert(
    id = id,
    account = account,
    category = category,
    payee = payee,
    notes = notes,
    date = date,
    amount = amount,
    isParent = isParent,
    parent = parent,
    error = error,
  )

private val ACCOUNTS = mapOf("a" to "Amex", "b" to "Barclays", "c" to "Chase")
private val PAYEES = mapOf("a" to "Argos", "b" to "B&Q", "c" to "Co-op")
private val CATEGORIES = mapOf("a" to "Additional", "b" to "Building", "c" to "Car")

// What the paging source should emit for a row added with insertTransaction
internal fun transaction(
  id: String,
  account: String,
  category: String?,
  payee: String,
  notes: String? = null,
  date: LocalDate = DATE_1,
  amount: Double = 123.45,
  balance: Double? = null,
) =
  Transaction(
    id = TransactionId(id),
    date = date,
    account = ACCOUNTS.getValue(account),
    payee = PAYEES.getValue(payee),
    notes = notes,
    category = category?.let(CATEGORIES::getValue),
    amount = Amount(amount),
    balance = balance?.let(::Amount),
    needsCategory = category == null,
  )

internal fun Transaction.withBalance(balance: Double) = copy(balance = Amount(balance))

internal fun Transaction.asChild() = copy(split = Child)

// What the paging source should emit for a split parent showing these children
internal fun Transaction.asParent(
  payee: String?,
  vararg children: Transaction,
  totalChildren: Int = children.size,
) =
  copy(
    payee = payee,
    category = null,
    needsCategory = false,
    split = Parent,
    children = children.map { it.asChild() }.toImmutableList(),
    totalChildren = totalChildren,
  )

internal val TRANSACTION_A = transaction(id = "a", account = "a", category = "a", payee = "a")
internal val TRANSACTION_B = transaction(id = "b", account = "b", category = "b", payee = "b")
internal val TRANSACTION_C = transaction(id = "c", account = "c", category = "c", payee = "c")

internal val DATED_A = TRANSACTION_A.copy(date = DATE_1)
internal val DATED_B = TRANSACTION_B.copy(date = DATE_1)
internal val DATED_C = TRANSACTION_C.copy(date = DATE_1)
internal val DATED_D =
  transaction(id = "d", account = "c", category = "c", payee = "c", date = DATE_2)
internal val DATED_E =
  transaction(id = "e", account = "c", category = "c", payee = "c", date = DATE_2)
internal val DATED_F =
  transaction(id = "f", account = "c", category = "c", payee = "c", date = DATE_3)
