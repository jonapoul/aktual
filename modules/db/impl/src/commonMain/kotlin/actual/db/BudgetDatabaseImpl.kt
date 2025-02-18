package actual.db

import actual.db.api.BudgetDatabase
import java.io.File

class BudgetDatabaseImpl(
  val db: RoomBudgetDatabase,
  override val budgetName: String,
  override val file: File,
) : BudgetDatabase {
  override fun preferences() = db.preferences()
  override fun spreadsheetCell() = db.spreadsheetCell()
  override fun transactions() = db.transactions()
}
