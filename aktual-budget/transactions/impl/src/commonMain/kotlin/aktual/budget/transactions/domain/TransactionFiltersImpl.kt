package aktual.budget.transactions.domain

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.ConditionSet
import aktual.budget.model.TransactionFilterId
import aktual.budget.model.TransactionSearch
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Inject
@SingleIn(BudgetScope::class)
@ContributesBinding(BudgetScope::class)
class TransactionFiltersImpl internal constructor() : TransactionFilters {
  private val mutableSearch = MutableStateFlow(TransactionSearch())
  override val search: StateFlow<TransactionSearch> = mutableSearch.asStateFlow()

  private val mutableSavedId = MutableStateFlow<TransactionFilterId?>(null)

  override val savedId: StateFlow<TransactionFilterId?> = mutableSavedId.asStateFlow()

  override fun apply(filter: SavedFilter) {
    mutableSearch.update { it.copy(conditions = filter.conditions) }
    mutableSavedId.update { filter.id }
  }

  override fun saved(id: TransactionFilterId) = mutableSavedId.update { id }

  override fun clearConditions() {
    mutableSearch.update { it.copy(conditions = ConditionSet(emptyList())) }
    mutableSavedId.update { null }
  }

  override fun reset() {
    mutableSearch.update { TransactionSearch() }
    mutableSavedId.update { null }
  }

  override fun setQuery(text: String) = mutableSearch.update { it.copy(text = text) }

  override fun addCondition(condition: Condition) = updateConditions { it + condition }

  override fun editCondition(index: Int, condition: Condition) = updateConditions { conditions ->
    if (index in conditions.indices) {
      conditions.toMutableList().apply { set(index, condition) }
    } else {
      conditions
    }
  }

  override fun removeCondition(index: Int) {
    updateConditions { conditions ->
      if (index in conditions.indices) {
        conditions.toMutableList().apply { removeAt(index) }
      } else {
        conditions
      }
    }
    if (search.value.conditions.conditions.isEmpty()) mutableSavedId.update { null }
  }

  override fun setConditionsOp(op: ConditionOp) = mutableSearch.update { current ->
    current.copy(conditions = current.conditions.copy(op = op))
  }

  private fun updateConditions(transform: (List<Condition>) -> List<Condition>) =
    mutableSearch.update { current ->
      current.copy(
        conditions = current.conditions.copy(conditions = transform(current.conditions.conditions)),
      )
    }
}
