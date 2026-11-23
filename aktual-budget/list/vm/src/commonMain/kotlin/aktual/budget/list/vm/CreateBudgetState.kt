package aktual.budget.list.vm

import aktual.budget.model.BudgetId
import aktual.budget.utils.UploadResult
import androidx.compose.runtime.Immutable

@Immutable
sealed interface CreateBudgetState {
  data object Idle : CreateBudgetState

  data object Creating : CreateBudgetState

  data object Failed : CreateBudgetState

  // Created and opened, but only on this device
  data class UploadFailed(val id: BudgetId, val failure: UploadResult.Failure) : CreateBudgetState
}
