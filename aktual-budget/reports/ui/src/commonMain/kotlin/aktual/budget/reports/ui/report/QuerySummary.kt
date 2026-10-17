package aktual.budget.reports.ui.report

import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.Field
import aktual.budget.reports.vm.FormulaQuery
import aktual.budget.reports.vm.FormulaReportMeta
import aktual.budget.reports.vm.TimeFrame
import aktual.budget.reports.vm.TimeFrameMode
import aktual.budget.reports.vm.editableKind
import aktual.core.l10n.Strings
import aktual.core.ui.formattedString
import aktual.core.ui.stringShort
import androidx.compose.runtime.Composable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

@Composable
internal fun FormulaQuery.conditionsSummary(names: ImmutableMap<String, String>): String {
  if (conditions.isEmpty()) return Strings.reportsFormulaViewerNoConditions
  val shown = conditions.take(MAX_SUMMARY_CONDITIONS).map { it.summary(names) }
  val hidden = conditions.size - shown.size
  val more = if (hidden > 0) Strings.reportsFormulaViewerMoreConditions(hidden) else null
  return (shown + listOfNotNull(more)).joinToString(SEPARATOR)
}

@Composable
private fun Condition.summary(names: ImmutableMap<String, String>): String {
  val field = field.label().replaceFirstChar { it.titlecase() }
  return listOfNotNull(field, operator.label(), valueLabel(names)).joinToString(" ")
}

@Composable
internal fun Condition.valueLabel(names: ImmutableMap<String, String>): String? =
  when (editableKind()) {
    Entity -> value.jsonPrimitive.contentOrNull?.let { names.nameOf(it) }
    EntityList -> (value as JsonArray).map { names.nameOf(it.jsonPrimitive.content) }.joinToString()
    Text,
    Day -> value.jsonPrimitive.content
    Money -> Amount(value.jsonPrimitive.long).formattedString()
    Flag ->
      if (value.jsonPrimitive.boolean) {
        Strings.reportsFormulaQueryTrue
      } else {
        Strings.reportsFormulaQueryFalse
      }
    NoValue -> null
    null -> value.toString()
  }

// A query with no time frame isn't filtered by date, so it shows as all time
@Composable
internal fun TimeFrame?.label(): String {
  if (this == null) return TimeFrameMode.Full.label()
  return if (mode == SlidingWindow || mode == Static) {
    "${start.stringShort()} - ${end.stringShort()}"
  } else {
    mode.label()
  }
}

private const val MAX_SUMMARY_CONDITIONS = 2
private const val SEPARATOR = " · "

@Suppress("MagicNumber", "StringLiteralDuplication")
internal val PREVIEW_FORMULA_META =
  FormulaReportMeta(
    name = "Groceries / month",
    formula = """=-QUERY("groceries") / 6""",
    colorFormula = """=IF(RESULT > 500, "theme_numberNegative", "theme_numberPositive")""",
    fontSizeMode = Static,
    staticFontSize = 32.0,
    showTitle = true,
    queries =
      mapOf(
        "groceries" to
          FormulaQuery(
            conditions =
              listOf(
                Condition(Category, OneOf, JsonArray(listOf(JsonPrimitive("food")))),
                Condition(Field.Amount, LessThan, JsonPrimitive(-1000)),
                Condition(Date, Is, buildJsonObject { put("frequency", "monthly") }),
              ),
            conditionsOp = And,
            timeFrame = TimeFrame(YearMonth(2025, 1), YearMonth(2025, 6), Static),
          ),
        "income" to
          FormulaQuery(
            conditions = listOf(Condition(Payee, Is, JsonPrimitive("employer"))),
            conditionsOp = Or,
            timeFrame = null,
          ),
      ),
  )
