package actual.core.files

import actual.budget.model.BudgetId
import okio.FileSystem
import okio.Path
import okio.Sink

interface DatabaseDirectory {
  val fileSystem: FileSystem
  fun pathFor(id: BudgetId): Path
  fun sink(id: BudgetId): Sink = fileSystem.sink(pathFor(id))
}
