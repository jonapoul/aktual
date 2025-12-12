package aktual.budget.transactions.vm

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Categories
import aktual.budget.db.Transactions
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.BudgetId
import aktual.budget.model.CategoryId
import aktual.budget.model.DbMetadata
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.core.model.LoginToken
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.time.Duration.Companion.days

internal val DATE_1 = LocalDate(2025, Month.JUNE, 1)
internal val DATE_2 = LocalDate(2025, Month.JUNE, 2)
internal val DATE_3 = LocalDate(2025, Month.JUNE, 3)

internal val TOKEN = LoginToken("abc-123")
internal val BUDGET_ID = BudgetId("xyz-789")
internal val METADATA = DbMetadata(data = persistentMapOf(DbMetadata.CloudFileId to BUDGET_ID))

internal val ID_A = TransactionId("a")
internal val ID_B = TransactionId("b")
internal val ID_C = TransactionId("c")
internal val ID_D = TransactionId("d")
internal val ID_E = TransactionId("e")
internal val ID_F = TransactionId("f")

internal val TRANSACTION_A = Transaction(ID_A, DATE_1, "Amex", "Argos", null, "Additional", Amount(123.45))
internal val TRANSACTION_B = Transaction(ID_B, DATE_2, "Barclays", "B&Q", null, "Building", Amount(123.45))
internal val TRANSACTION_C = Transaction(ID_C, DATE_3, "Chase", "Co-op", null, "Car", Amount(123.45))

internal suspend fun BudgetDatabase.insertAccount(id: AccountId, name: String) {
  accountsQueries.withoutResult {
    insert(
      id = id,
      account_id = id.toString(),
      name = name,
      official_name = name,
      bank = null,
      offbudget = false,
      account_sync_source = null,
    )
  }
}

internal suspend fun BudgetDatabase.insertPayee(id: PayeeId, name: String) {
  payeesQueries.withoutResult {
    insert(
      id = id,
      name = name,
      category = null,
      tombstone = false,
      transfer_acct = null,
      favorite = false,
      learn_categories = null,
    )
  }
}

internal suspend fun BudgetDatabase.insertCategory(id: CategoryId, name: String) {
  categoriesQueries.withoutResult {
    insert(
      Categories(
        id = id,
        name = name,
        is_income = false,
        cat_group = null,
        sort_order = null,
        tombstone = false,
        hidden = false,
        goal_def = null,
      ),
    )
  }
}

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
