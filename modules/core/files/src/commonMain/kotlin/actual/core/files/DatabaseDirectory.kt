package actual.core.files

import actual.budget.model.BudgetId
import okio.Path

interface DatabaseDirectory {
  fun pathFor(id: BudgetId): Path
}
