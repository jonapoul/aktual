package aktual.budget.reports.ui.report

import aktual.budget.reports.vm.FormulaEdits
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface EditFormulaAction {
  data object Close : EditFormulaAction

  data object Save : EditFormulaAction

  data object EditName : EditFormulaAction

  data object EditFormula : EditFormulaAction

  data object EditColorRule : EditFormulaAction

  data object EditFontSize : EditFormulaAction

  @JvmInline value class Edit(val transform: (FormulaEdits) -> FormulaEdits) : EditFormulaAction
}
