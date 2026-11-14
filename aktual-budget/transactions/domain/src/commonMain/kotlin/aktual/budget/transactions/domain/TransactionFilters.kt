package aktual.budget.transactions.domain

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.ConditionSet
import aktual.budget.model.TransactionFilterId
import aktual.budget.model.TransactionSearch
import aktual.di.BudgetScope
import alakazam.kotlin.StateHolder
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// The search narrowing the account lists. Shared by them all, so it stays put when switching
// accounts, and cleared when the budget closes
@Inject
@SingleIn(BudgetScope::class)
class TransactionFilters internal constructor() :
  StateHolder<TransactionSearch>(TransactionSearch()) {
  private val mutableSavedId = MutableStateFlow<TransactionFilterId?>(null)

  // The saved filter last applied or saved. Its conditions may since have changed, and it's
  // forgotten once they're all removed
  val savedId: StateFlow<TransactionFilterId?> = mutableSavedId.asStateFlow()

  // Replaces the conditions with the saved filter's, keeping the text
  fun apply(filter: SavedFilter) {
    update { it.copy(conditions = filter.conditions) }
    mutableSavedId.update { filter.id }
  }

  // After the current conditions are saved as [id]
  fun saved(id: TransactionFilterId) = mutableSavedId.update { id }

  fun clearConditions() {
    update { it.copy(conditions = ConditionSet(emptyList())) }
    mutableSavedId.update { null }
  }

  override fun reset() {
    super.reset()
    mutableSavedId.update { null }
  }

  fun setQuery(text: String) = update { it.copy(text = text) }

  fun addCondition(condition: Condition) = updateConditions { it + condition }

  // Ignored if index is out of range, e.g. after a removal raced the edit
  fun editCondition(index: Int, condition: Condition) = updateConditions { conditions ->
    if (index in conditions.indices) {
      conditions.toMutableList().apply { set(index, condition) }
    } else {
      conditions
    }
  }

  fun removeCondition(index: Int) {
    updateConditions { conditions ->
      if (index in conditions.indices) {
        conditions.toMutableList().apply { removeAt(index) }
      } else {
        conditions
      }
    }
    if (value.conditions.conditions.isEmpty()) mutableSavedId.update { null }
  }

  fun setConditionsOp(op: ConditionOp) = update { search ->
    search.copy(conditions = search.conditions.copy(op = op))
  }

  private fun updateConditions(transform: (List<Condition>) -> List<Condition>) = update { search ->
    search.copy(
      conditions = search.conditions.copy(conditions = transform(search.conditions.conditions)),
    )
  }
}
