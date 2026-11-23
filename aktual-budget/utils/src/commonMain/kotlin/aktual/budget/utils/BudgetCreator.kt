package aktual.budget.utils

import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata

/** Makes a new local-only budget in the directory for [id]. */
fun interface BudgetCreator {
  /** Returns the new budget's metadata, or null if it couldn't be created. */
  suspend operator fun invoke(id: BudgetId, name: String): DbMetadata?
}
