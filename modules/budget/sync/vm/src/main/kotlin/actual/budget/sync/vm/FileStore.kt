package actual.budget.sync.vm

import actual.budget.model.BudgetId
import java.io.File

interface FileStore {
  fun budgetFile(id: BudgetId): File
}
