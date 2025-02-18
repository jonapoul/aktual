package actual.db.api

import java.io.File

interface BudgetDatabase {
  val budgetName: String
  val file: File

  fun preferences(): PreferencesDao
  fun spreadsheetCell(): SpreadsheetCellDao
  fun transactions(): TransactionDao
}
