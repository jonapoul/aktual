package aktual.budget.reports.vm.report

import aktual.budget.reports.vm.FormulaData
import aktual.budget.reports.vm.FormulaEdits
import androidx.compose.runtime.Immutable

@Immutable
sealed interface FormulaReportState {
  data object Loading : FormulaReportState

  data object NotFound : FormulaReportState

  // The draft matches the saved card unless editing. The preview is null until the draft's first
  // evaluation finishes
  data class Loaded(
    val draft: FormulaEdits,
    val preview: FormulaData?,
    val isEditing: Boolean,
    val hasChanges: Boolean,
  ) : FormulaReportState
}
