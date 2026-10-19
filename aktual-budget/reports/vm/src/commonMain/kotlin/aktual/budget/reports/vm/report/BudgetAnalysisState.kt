package aktual.budget.reports.vm.report

import aktual.budget.reports.vm.BudgetAnalysisData
import aktual.budget.reports.vm.BudgetAnalysisSettings
import androidx.compose.runtime.Immutable

@Immutable
sealed interface BudgetAnalysisState {
  data object Loading : BudgetAnalysisState

  data object NotFound : BudgetAnalysisState

  // The draft keeps its changes after the editor closes, until they're saved or reverted. The
  // preview is null until the draft's first chart loads
  data class Loaded(
    val name: String?,
    val draft: BudgetAnalysisSettings,
    val preview: BudgetAnalysisData?,
    val isEditing: Boolean,
    val hasChanges: Boolean,
  ) : BudgetAnalysisState
}
