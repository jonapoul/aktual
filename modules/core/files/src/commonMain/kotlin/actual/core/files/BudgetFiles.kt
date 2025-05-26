package actual.core.files

import actual.budget.model.BudgetId
import okio.Path

interface BudgetFiles {
  fun directory(id: BudgetId, mkdirs: Boolean = false): Path
}

fun BudgetFiles.database(id: BudgetId, mkdirs: Boolean = false): Path = directory(id, mkdirs).resolve("db.sqlite")

fun BudgetFiles.metadata(id: BudgetId, mkdirs: Boolean = false): Path = directory(id, mkdirs).resolve("metadata.json")
