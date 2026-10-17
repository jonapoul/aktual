package aktual.budget.reports.ui.report

import aktual.budget.reports.vm.FormulaEdits
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface FormulaReportAction {
  data object Back : FormulaReportAction

  data object StartEditing : FormulaReportAction

  data object StopEditing : FormulaReportAction

  data object Save : FormulaReportAction

  @JvmInline value class Edit(val transform: (FormulaEdits) -> FormulaEdits) : FormulaReportAction

  data object EditName : FormulaReportAction

  data object EditFormula : FormulaReportAction

  data object EditColorRule : FormulaReportAction

  data object EditFontSize : FormulaReportAction

  data object AddQuery : FormulaReportAction

  @JvmInline value class EditQuery(val name: String) : FormulaReportAction
}
