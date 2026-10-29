package aktual.budget.reports.ui.report

import aktual.budget.reports.vm.BudgetAnalysisSettings
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface BudgetAnalysisAction {
  @JvmInline
  value class Edit(val transform: (BudgetAnalysisSettings) -> BudgetAnalysisSettings) :
    BudgetAnalysisAction

  @JvmInline value class Rename(val name: String) : BudgetAnalysisAction
}
