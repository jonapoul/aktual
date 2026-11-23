package aktual.budget.list.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface CreateBudgetState {
  data object Idle : CreateBudgetState

  data object Creating : CreateBudgetState

  data object Failed : CreateBudgetState
}
