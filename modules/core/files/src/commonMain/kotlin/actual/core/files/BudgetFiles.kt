package actual.core.files

import actual.budget.model.BudgetId
import okio.FileSystem
import okio.Path

interface BudgetFiles {
  val fileSystem: FileSystem
  fun directory(id: BudgetId, mkdirs: Boolean = false): Path
}

fun BudgetFiles.database(id: BudgetId, mkdirs: Boolean = false): Path = directory(id, mkdirs).resolve("db.sqlite")

fun BudgetFiles.metadata(id: BudgetId, mkdirs: Boolean = false): Path = directory(id, mkdirs).resolve("metadata.json")

fun BudgetFiles.zip(id: BudgetId, mkdirs: Boolean = false): Path = directory(id, mkdirs).resolve("download.zip")
