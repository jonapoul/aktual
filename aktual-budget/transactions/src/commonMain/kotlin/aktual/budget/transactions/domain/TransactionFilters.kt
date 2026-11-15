package aktual.budget.transactions.domain

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.TransactionFilterId
import aktual.budget.model.TransactionSearch
import kotlinx.coroutines.flow.StateFlow

// The search narrowing the account lists. Shared by them all, so it stays put when switching
// accounts, and cleared when the budget closes
@Suppress("ComplexInterface")
interface TransactionFilters {
  val search: StateFlow<TransactionSearch>

  // The saved filter last applied or saved. Its conditions may since have changed, and it's
  // forgotten once they're all removed
  val savedId: StateFlow<TransactionFilterId?>

  // Replaces the conditions with the saved filter's, keeping the text
  fun apply(filter: SavedFilter)

  // After the current conditions are saved as [id]
  fun saved(id: TransactionFilterId)

  fun clearConditions()

  fun reset()

  fun setQuery(text: String)

  fun addCondition(condition: Condition)

  // Ignored if index is out of range, e.g. after a removal raced the edit
  fun editCondition(index: Int, condition: Condition)

  fun removeCondition(index: Int)

  fun setConditionsOp(op: ConditionOp)
}
