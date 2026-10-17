package aktual.budget.reports.ui.report

import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.Field
import aktual.budget.reports.ui.charts.PREVIEW_FORMULA_DATA
import aktual.budget.reports.ui.charts.resolveColor
import aktual.budget.reports.vm.ConditionValueKind
import aktual.budget.reports.vm.FormulaData
import aktual.budget.reports.vm.FormulaQuery
import aktual.budget.reports.vm.FormulaReportMeta
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.TimeFrame
import aktual.budget.reports.vm.TimeFrameMode
import aktual.budget.reports.vm.editableKind
import aktual.core.icons.material.ChevronRight
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.formattedString
import aktual.core.ui.stringShort
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily.Companion.Monospace
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
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

// A read-only summary of a formula card's settings. Every row opens the editor
@Composable
internal fun FormulaViewer(
  meta: FormulaReportMeta,
  data: FormulaData,
  queryEditorData: QueryEditorData?,
  onEdit: () -> Unit,
  modifier: Modifier = Modifier,
) =
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
    Section(label = Strings.reportsFormulaEditorFormula) {
      ViewerRow(modifier = Modifier.heightIn(min = 52.dp), onClick = onEdit) {
        val formula = meta.formula?.takeIf { it.isNotBlank() }
        if (formula == null) {
          Text(
            modifier = Modifier.weight(1f),
            text = Strings.reportsFormulaViewerNotSet,
            style = typography.bodyMedium,
            color = colors.tableHeaderText,
          )
        } else {
          Text(
            modifier = Modifier.weight(1f),
            text = formula,
            style = typography.bodyMedium.copy(fontFamily = Monospace),
            color = colors.pageTextPositive,
            maxLines = 1,
            overflow = Ellipsis,
          )
        }
      }
    }

    Section(label = Strings.reportsFormulaViewerAppearance) {
      ValueRow(label = Strings.reportsFormulaViewerColorRule, onClick = onEdit) {
        val color = data.resolveColor()
        if (color != null) {
          Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        }
        Value(
          text =
            meta.colorFormula?.takeIf { it.isNotBlank() } ?: Strings.reportsFormulaViewerNotSet,
          monospace = !meta.colorFormula.isNullOrBlank(),
        )
      }
      Divider()
      ValueRow(label = Strings.reportsFormulaEditorFontSize, onClick = onEdit) {
        Value(text = meta.fontSizeString())
      }
      Divider()
      ValueRow(label = Strings.reportsFormulaEditorShowTitle, onClick = null) {
        Value(
          text =
            if (meta.showTitle != false) {
              Strings.reportsFormulaViewerOn
            } else {
              Strings.reportsFormulaViewerOff
            },
        )
      }
    }

    Section(label = Strings.reportsFormulaViewerQueries(meta.queries.size)) {
      if (meta.queries.isEmpty()) {
        ViewerRow(onClick = onEdit) {
          Text(
            modifier = Modifier.weight(1f),
            text = Strings.reportsFormulaQueriesEmpty,
            style = typography.bodyMedium,
            color = colors.tableHeaderText,
          )
        }
      }

      meta.queries.entries.forEachIndexed { index, (name, query) ->
        if (index > 0) Divider()
        QueryRow(name = name, query = query, names = queryEditorData?.names, onClick = onEdit)
      }
    }
  }

@Composable
private fun Section(label: String, content: @Composable ColumnScope.() -> Unit) = Column {
  Text(
    modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
    text = label.uppercase(),
    style = typography.labelMedium.copy(fontWeight = SemiBold, letterSpacing = 0.08.em),
    color = colors.tableHeaderText,
  )
  Column(
    modifier = Modifier.fillMaxWidth().clip(CardShape).background(colors.tableBackground),
    content = content,
  )
}

@Composable
private fun ViewerRow(
  onClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
  verticalAlignment: Alignment.Vertical = CenterVertically,
  content: @Composable RowScope.() -> Unit,
) =
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)
        .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
        .padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
    verticalAlignment = verticalAlignment,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    content()
    if (onClick == null) {
      Box(modifier = Modifier.size(CHEVRON_SIZE))
    } else {
      Icon(
        modifier = Modifier.size(CHEVRON_SIZE),
        imageVector = MaterialIcons.ChevronRight,
        contentDescription = null,
        tint = colors.tableHeaderText,
      )
    }
  }

@Composable
private fun ValueRow(
  label: String,
  onClick: (() -> Unit)?,
  value: @Composable RowScope.() -> Unit,
) =
  ViewerRow(onClick = onClick) {
    Text(text = label, style = typography.bodyLarge, color = colors.tableText)
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
      verticalAlignment = CenterVertically,
      content = value,
    )
  }

@Composable
private fun Value(text: String, monospace: Boolean = false) =
  Text(
    text = text,
    style =
      if (monospace) typography.bodySmall.copy(fontFamily = Monospace) else typography.bodyMedium,
    color = colors.tableHeaderText,
    maxLines = 1,
    overflow = Ellipsis,
  )

@Composable
private fun QueryRow(
  name: String,
  query: FormulaQuery,
  names: ImmutableMap<String, String>?,
  onClick: () -> Unit,
) =
  ViewerRow(onClick = onClick, verticalAlignment = Top) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(
        text = name,
        style = typography.bodyLarge.copy(fontFamily = Monospace, fontWeight = Medium),
        color = colors.tableText,
      )
      // Entity names load separately, so the summary waits for them rather than showing ids
      if (names != null) {
        QueryDetail(text = query.conditionsSummary(names))
      }
      QueryDetail(text = query.timeFrame.label())
    }
  }

@Composable
private fun QueryDetail(text: String) =
  Text(text = text, style = typography.bodySmall, color = colors.tableHeaderText)

@Composable
private fun Divider() =
  Box(
    modifier =
      Modifier.padding(horizontal = 16.dp)
        .fillMaxWidth()
        .height(1.dp)
        .background(colors.tableBorder),
  )

@Composable
private fun FormulaReportMeta.fontSizeString(): String {
  if (fontSizeMode != Static) return Strings.reportsFormulaEditorFontDynamic
  val size = staticFontSize ?: DEFAULT_STATIC_SIZE
  val text = if (size % 1.0 == 0.0) size.toLong().toString() else size.toString()
  return Strings.reportsFormulaViewerStaticSize(text)
}

@Composable
private fun FormulaQuery.conditionsSummary(names: ImmutableMap<String, String>): String {
  if (conditions.isEmpty()) return Strings.reportsFormulaViewerNoConditions
  val shown = conditions.take(MAX_SUMMARY_CONDITIONS).map { it.summary(names) }
  val hidden = conditions.size - shown.size
  val more = if (hidden > 0) Strings.reportsFormulaViewerMoreConditions(hidden) else null
  return (shown + listOfNotNull(more)).joinToString(SEPARATOR)
}

@Composable
private fun Condition.summary(names: ImmutableMap<String, String>): String {
  val value =
    when (editableKind()) {
      Entity -> value.jsonPrimitive.contentOrNull?.let { names.nameOf(it) }
      EntityList ->
        (value as JsonArray).map { names.nameOf(it.jsonPrimitive.content) }.joinToString()
      ConditionValueKind.Text,
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
  val field = field.label().replaceFirstChar { it.titlecase() }
  return listOfNotNull(field, operator.label(), value).joinToString(" ")
}

// A query with no time frame isn't filtered by date, so it shows as all time
@Composable
private fun TimeFrame?.label(): String {
  if (this == null) return TimeFrameMode.Full.label()
  return if (mode == SlidingWindow || mode == Static) {
    "${start.stringShort()} - ${end.stringShort()}"
  } else {
    mode.label()
  }
}

private val CHEVRON_SIZE = 20.dp
private const val MAX_SUMMARY_CONDITIONS = 2
private const val SEPARATOR = " · "
private const val DEFAULT_STATIC_SIZE = 32.0

@Preview
@Composable
private fun PreviewFormulaViewer(
  @PreviewParameter(FormulaViewerProvider::class) params: ColoredParams<FormulaReportMeta>,
) =
  PreviewWithColoredParams(params) {
    FormulaViewer(
      modifier = Modifier.padding(8.dp),
      meta = this,
      data = PREVIEW_FORMULA_DATA,
      queryEditorData = PREVIEW_QUERY_DATA,
      onEdit = {},
    )
  }

private class FormulaViewerProvider :
  ColoredParameterProvider<FormulaReportMeta>(PREVIEW_FORMULA_META, FormulaReportMeta())

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
