package actual.core.files

import actual.budget.model.BudgetId
import java.io.File

interface FileSystem {
  fun databaseDirectory(): File
  fun budgetDatabase(id: BudgetId): File
}
