package aktual.budget.reports.vm.report

import aktual.budget.reports.vm.FormulaData
import aktual.budget.reports.vm.FormulaEdits
import androidx.compose.runtime.Immutable

@Immutable
sealed interface EditFormulaReportState {
  data object Loading : EditFormulaReportState

  data object NotFound : EditFormulaReportState

  // The preview is null until the draft's first evaluation finishes
  data class Loaded(
    val draft: FormulaEdits,
    val preview: FormulaData?,
    val hasChanges: Boolean,
  ) : EditFormulaReportState
}
