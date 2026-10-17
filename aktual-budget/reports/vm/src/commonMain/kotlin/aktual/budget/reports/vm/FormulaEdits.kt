package aktual.budget.reports.vm

import androidx.compose.runtime.Immutable

@Immutable
data class FormulaEdits(
  val formula: String,
  val colorFormula: String,
  val fontSizeMode: FontSizeMode,
  val staticFontSize: Double,
  val showTitle: Boolean,
)
