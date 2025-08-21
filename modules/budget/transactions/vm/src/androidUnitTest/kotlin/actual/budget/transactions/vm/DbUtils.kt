package actual.budget.transactions.vm

import actual.core.model.LoginToken
import actual.budget.db.BudgetDatabase
import actual.budget.db.Categories
import actual.budget.db.Transactions
import actual.budget.db.withoutResult
import actual.budget.model.AccountId
import actual.budget.model.Amount
import actual.budget.model.BudgetId
import actual.budget.model.CategoryId
import actual.budget.model.DbMetadata
import actual.budget.model.PayeeId
import actual.budget.model.TransactionId
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.time.Duration.Companion.days

internal val DATE_1 = LocalDate(2025, Month.JUNE, 1)
internal val DATE_2 = LocalDate(2025, Month.JUNE, 2)
internal val DATE_3 = LocalDate(2025, Month.JUNE, 3)

internal val TOKEN = LoginToken("abc-123")
internal val BUDGET_ID = BudgetId("xyz-789")
internal val METADATA = DbMetadata(data = persistentMapOf("cloudFileId" to BUDGET_ID.value))

internal val ID_A = TransactionId("a")
internal val ID_B = TransactionId("b")
internal val ID_C = TransactionId("c")

internal val TRANSACTION_A = Transaction(ID_A, DATE_1, "Amex", "Argos", null, "Additional", Amount(123.45))
internal val TRANSACTION_B = Transaction(ID_B, DATE_2, "Barclays", "B&Q", null, "Building", Amount(123.45))
internal val TRANSACTION_C = Transaction(ID_C, DATE_3, "Chase", "Co-op", null, "Car", Amount(123.45))

internal suspend fun BudgetDatabase.insertAccount(id: AccountId, name: String) {
  accountsQueries.withoutResult { insert(id, id.toString(), name, name, null, false, null) }
}

internal suspend fun BudgetDatabase.insertPayee(id: PayeeId, name: String) {
  payeesQueries.withoutResult { insert(id, name, null, false, null, false, null) }
}

internal suspend fun BudgetDatabase.insertCategory(id: CategoryId, name: String) {
  categoriesQueries.withoutResult { insert(Categories(id, name, false, null, null, false, false, null)) }
}

internal fun persistentListOfIds(vararg ids: String) = ids.map(::TransactionId).toPersistentList()

internal suspend fun BudgetDatabase.insertTransaction(
  id: String,
  account: String,
  category: String,
  payee: String,
  notes: String? = null,
  date: LocalDate = DATE_1,
  amount: Double = 123.45,
) = transactionsQueries.withoutResult {
  insert(
    Transactions(
      id = TransactionId(id),
      isParent = false,
      isChild = false,
      acct = AccountId(account),
      category = CategoryId(category),
      amount = Amount(amount),
      description = PayeeId(payee),
      notes = notes,
      date = date,
      financial_id = null,
      type = null,
      location = null,
      error = null,
      imported_description = null,
      starting_balance_flag = null,
      transferred_id = null,
      sort_order = date
        .toEpochDays()
        .days
        .inWholeMilliseconds
        .toDouble(),
      tombstone = null,
      cleared = null,
      pending = null,
      parent_id = null,
      schedule = null,
      reconciled = null,
      raw_synced_data = null,
    ),
  )
}
