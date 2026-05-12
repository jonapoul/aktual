package aktual.budget.transactions.vm

import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.BudgetId
import aktual.budget.model.CategoryId
import aktual.budget.model.DbMetadata
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.core.model.Token
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

internal val DATE_1 = LocalDate(2025, Month.JUNE, 1)
internal val DATE_2 = LocalDate(2025, Month.JUNE, 2)
internal val DATE_3 = LocalDate(2025, Month.JUNE, 3)

internal val TOKEN = Token("abc-123")
internal val BUDGET_ID = BudgetId("xyz-789")
internal val METADATA = DbMetadata(data = persistentMapOf(DbMetadata.CloudFileId to BUDGET_ID))

internal val ID_A = TransactionId("a")
internal val ID_B = TransactionId("b")
internal val ID_C = TransactionId("c")
internal val ID_D = TransactionId("d")
internal val ID_E = TransactionId("e")
internal val ID_F = TransactionId("f")

internal suspend fun AccountDao.insertAccount(id: AccountId, name: String) {
  insert(id = id, accountId = id.toString(), name = name, officialName = name)
}

internal suspend fun PayeeDao.insertPayee(id: PayeeId, name: String) = insert(id, name)

internal suspend fun CategoryDao.insertCategory(id: CategoryId, name: String) = insert(id, name)

internal suspend fun TransactionDao.insertTransaction(
  id: String,
  account: String,
  category: String,
  payee: String,
  notes: String? = null,
  date: LocalDate = DATE_1,
  amount: Double = 123.45,
) =
  insert(
    id = id,
    account = account,
    category = category,
    payee = payee,
    notes = notes,
    date = date,
    amount = amount,
  )
