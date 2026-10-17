package aktual.budget.reports.vm

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap

@Immutable
data class FormulaEdits(
  val name: String,
  val formula: String,
  val colorFormula: String,
  val fontSizeMode: FontSizeMode,
  val staticFontSize: Double,
  val showTitle: Boolean,
  val queries: ImmutableMap<String, FormulaQuery>,
)

fun FormulaReportMeta.toEdits() =
  FormulaEdits(
    name = name.orEmpty(),
    formula = formula.orEmpty(),
    colorFormula = colorFormula.orEmpty(),
    fontSizeMode = if (fontSizeMode == Static) Static else Dynamic,
    staticFontSize = staticFontSize ?: DEFAULT_STATIC_FONT_SIZE,
    showTitle = showTitle != false,
    queries = queries.toImmutableMap(),
  )

fun FormulaReportMeta.withEdits(edits: FormulaEdits) =
  copy(
    name = edits.name,
    formula = edits.formula,
    colorFormula = edits.colorFormula,
    fontSizeMode = edits.fontSizeMode,
    staticFontSize = edits.staticFontSize,
    showTitle = edits.showTitle,
    queries = edits.queries,
  )

const val DEFAULT_STATIC_FONT_SIZE = 32.0
