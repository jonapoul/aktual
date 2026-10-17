package aktual.budget.reports.vm

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap

@Immutable
data class FormulaEdits(
  val formula: String,
  val colorFormula: String,
  val fontSizeMode: FontSizeMode,
  val staticFontSize: Double,
  val showTitle: Boolean,
  val queries: ImmutableMap<String, FormulaQuery>,
)
