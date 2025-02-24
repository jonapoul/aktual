package actual.api.download

import actual.budget.model.BudgetId
import java.io.File

interface FileStore {
  fun budgetFile(id: BudgetId): File
}
