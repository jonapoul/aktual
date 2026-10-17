package aktual.budget.reports.vm

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.YearMonth

@Immutable
data class FormulaEdits(
  // Null leaves the stored name as it is
  val name: String?,
  val formula: String,
  val colorFormula: String,
  val fontSizeMode: FontSizeMode,
  val staticFontSize: Double,
  val showTitle: Boolean,
  val queries: ImmutableMap<String, FormulaQuery>,
)

fun FormulaReportMeta.toEdits() =
  FormulaEdits(
    name = name,
    formula = formula.orEmpty(),
    colorFormula = colorFormula.orEmpty(),
    fontSizeMode = if (fontSizeMode == Static) Static else Dynamic,
    staticFontSize = staticFontSize ?: DEFAULT_STATIC_FONT_SIZE,
    showTitle = showTitle != false,
    queries = queries.toImmutableMap(),
  )

fun FormulaReportMeta.withEdits(edits: FormulaEdits) =
  copy(
    name = edits.name ?: name,
    formula = edits.formula,
    colorFormula = edits.colorFormula,
    fontSizeMode = edits.fontSizeMode,
    staticFontSize = edits.staticFontSize,
    showTitle = edits.showTitle,
    queries = edits.queries,
  )

// Upstream starts a new query on the current month, moving with time
fun FormulaEdits.withNewQuery(name: String, month: YearMonth): FormulaEdits =
  withQuery(
    name,
    FormulaQuery(
      conditions = emptyList(),
      conditionsOp = And,
      timeFrame = TimeFrame(month, month, SlidingWindow),
    ),
  )

fun FormulaEdits.withQuery(name: String, query: FormulaQuery): FormulaEdits =
  copy(queries = (queries + (name to query)).toImmutableMap())

fun FormulaEdits.withQueryRenamed(from: String, to: String): FormulaEdits =
  copy(queries = queries.renamed(from, to))

fun FormulaEdits.withoutQuery(name: String): FormulaEdits =
  copy(queries = (queries - name).toImmutableMap())

const val DEFAULT_STATIC_FONT_SIZE = 32.0
