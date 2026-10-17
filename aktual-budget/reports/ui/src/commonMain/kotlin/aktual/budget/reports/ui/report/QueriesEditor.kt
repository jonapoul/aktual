package aktual.budget.reports.ui.report

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.reports.ui.dashboard.NameDialog
import aktual.budget.reports.ui.montecarlo.LabeledField
import aktual.budget.reports.vm.FormulaQuery
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.QueryEntity
import aktual.budget.reports.vm.TimeFrame
import aktual.budget.reports.vm.TimeFrameMode
import aktual.budget.reports.vm.newQueryCondition
import aktual.budget.reports.vm.renamed
import aktual.core.icons.material.Edit
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.YearMonthPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// packages/desktop-client/src/components/formula/QueryManager.tsx
@Composable
internal fun QueriesEditor(
  queries: ImmutableMap<String, FormulaQuery>,
  data: QueryEditorData,
  onChange: (ImmutableMap<String, FormulaQuery>) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showAddDialog by remember { mutableStateOf(false) }
  val addedNames = remember { mutableSetOf<String>() }

  LabeledField(
    modifier = modifier,
    label = Strings.reportsFormulaQueries,
    help = Strings.reportsFormulaQueriesHelp,
  ) {
    // Each item holds its own spacing, so a removed one takes its gap with it as it shrinks
    Column(modifier = Modifier.fillMaxWidth()) {
      if (queries.isEmpty()) {
        Text(
          modifier = Modifier.padding(bottom = 8.dp),
          text = Strings.reportsFormulaQueriesEmpty,
          style = typography.bodyMedium,
          color = colors.pageTextSubdued,
        )
      }

      queries.forEach { (name, query) ->
        key(name) {
          RemovableItem(
            isNew = name in addedNames,
            onRemove = { onChange((queries - name).toImmutableMap()) },
          ) { remove ->
            QueryCard(
              modifier = Modifier.padding(bottom = 8.dp),
              name = name,
              query = query,
              data = data,
              isNameFree = { it !in queries },
              onChange = { onChange((queries + (name to it)).toImmutableMap()) },
              onRename = { newName -> onChange(queries.renamed(name, newName)) },
              onDelete = remove,
            )
          }
        }
      }

      NormalTextButton(
        modifier = Modifier.fillMaxWidth(),
        text = Strings.reportsFormulaQueryAdd,
        onClick = { showAddDialog = true },
      )
    }
  }

  if (showAddDialog) {
    NameDialog(
      title = Strings.reportsFormulaQueryNew,
      placeholder = Strings.reportsFormulaQueryName,
      confirmText = Strings.reportsFormulaQueryCreate,
      capitalization = None,
      isValid = { it !in queries },
      onDismiss = { showAddDialog = false },
      onConfirm = { name ->
        showAddDialog = false
        addedNames += name
        onChange((queries + (name to newQuery(data))).toImmutableMap())
      },
    )
  }
}

@Composable
private fun QueryCard(
  name: String,
  query: FormulaQuery,
  data: QueryEditorData,
  isNameFree: (String) -> Boolean,
  onChange: (FormulaQuery) -> Unit,
  onRename: (String) -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showRenameDialog by remember { mutableStateOf(false) }
  val conditionKeys = remember { ItemKeys(query.conditions.size) }

  Column(
    modifier = modifier.fillMaxWidth().background(colors.tableBackground, CardShape).padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Row(verticalAlignment = CenterVertically) {
      Text(modifier = Modifier.weight(1f), text = name, style = typography.titleSmall)
      BareIconButton(
        imageVector = MaterialIcons.Edit,
        contentDescription = Strings.reportsFormulaQueryRename,
        onClick = { showRenameDialog = true },
      )
      ConfirmDeleteButton(
        contentDescription = Strings.reportsFormulaQueryDelete,
        onDelete = onDelete,
      )
    }

    Column {
      AktualSlidingToggleButton(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        selected = if (query.conditionsOp == Or) Or else And,
        options = CONDITION_OPS,
        onSelect = { op -> onChange(query.copy(conditionsOp = op)) },
        string = { op ->
          if (op == Or) Strings.reportsFormulaQueryMatchAny else Strings.reportsFormulaQueryMatchAll
        },
      )

      query.conditions.fastForEachIndexed { index, condition ->
        key(conditionKeys[index]) {
          RemovableItem(
            isNew = conditionKeys.isNew(index),
            onRemove = {
              conditionKeys.removed(index)
              val conditions = query.conditions.toMutableList().apply { removeAt(index) }
              onChange(query.copy(conditions = conditions))
            },
          ) { remove ->
            QueryConditionEditor(
              modifier = Modifier.padding(bottom = 8.dp),
              condition = condition,
              data = data,
              onChange = { changed ->
                val conditions = query.conditions.toMutableList().apply { set(index, changed) }
                onChange(query.copy(conditions = conditions))
              },
              onDelete = remove,
            )
          }
        }
      }

      NormalTextButton(
        modifier = Modifier.fillMaxWidth(),
        text = Strings.reportsFormulaQueryAddCondition,
        onClick = {
          conditionKeys.added()
          onChange(query.copy(conditions = query.conditions + newQueryCondition(Payee)))
        },
      )
    }

    TimeFrameEditor(
      timeFrame = query.timeFrame,
      data = data,
      onChange = { timeFrame -> onChange(query.copy(timeFrame = timeFrame)) },
    )
  }

  if (showRenameDialog) {
    NameDialog(
      title = Strings.reportsFormulaQueryRename,
      placeholder = Strings.reportsFormulaQueryName,
      confirmText = Strings.reportsFormulaQueryRenameConfirm,
      initialName = name,
      capitalization = None,
      isValid = { it == name || isNameFree(it) },
      onDismiss = { showRenameDialog = false },
      onConfirm = { newName ->
        showRenameDialog = false
        onRename(newName)
      },
    )
  }
}

// A query with no time frame isn't filtered by date, so it shows as all time until changed
@Composable
private fun TimeFrameEditor(
  timeFrame: TimeFrame?,
  data: QueryEditorData,
  onChange: (TimeFrame) -> Unit,
) =
  LabeledField(label = Strings.reportsFormulaQueryTimeFrame) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      val current = data.currentMonth
      AktualExposedDropDownMenu(
        modifier = Modifier.fillMaxWidth(),
        value = timeFrame?.mode ?: Full,
        onValueChange = { mode ->
          onChange(timeFrame?.copy(mode = mode) ?: TimeFrame(current, current, mode))
        },
        options = TIME_FRAME_MODES,
        string = { mode -> mode.label() },
        textStyle = typography.bodySmall,
      )

      if (timeFrame != null && (timeFrame.mode == SlidingWindow || timeFrame.mode == Static)) {
        val range =
          YearMonthRange(
            minOf(data.months.start, timeFrame.start),
            maxOf(data.months.endInclusive, timeFrame.end),
          )
        MonthRow(label = Strings.reportsFormulaQueryStart) {
          YearMonthPicker(
            value = timeFrame.start,
            range = range,
            onValueChange = { start ->
              onChange(timeFrame.copy(start = start, end = maxOf(start, timeFrame.end)))
            },
          )
        }
        MonthRow(label = Strings.reportsFormulaQueryEnd) {
          YearMonthPicker(
            value = timeFrame.end,
            range = range,
            onValueChange = { end ->
              onChange(timeFrame.copy(start = minOf(end, timeFrame.start), end = end))
            },
          )
        }
      }
    }
  }

@Composable
private fun MonthRow(label: String, picker: @Composable () -> Unit) =
  Row(verticalAlignment = CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(modifier = Modifier.weight(1f), text = label, style = typography.bodyMedium)
    picker()
  }

// Upstream starts a new query on the current month, moving with time
private fun newQuery(data: QueryEditorData) =
  FormulaQuery(
    conditions = emptyList(),
    conditionsOp = And,
    timeFrame = TimeFrame(data.currentMonth, data.currentMonth, SlidingWindow),
  )

private val CONDITION_OPS = persistentListOf(ConditionOp.And, ConditionOp.Or)

private val TIME_FRAME_MODES = TimeFrameMode.entries.filter { it != Unknown }.toImmutableList()

@Preview
@Composable
private fun PreviewQueriesEditor(
  @PreviewParameter(QueriesEditorProvider::class)
  params: ColoredParams<ImmutableMap<String, FormulaQuery>>,
) =
  PreviewWithColoredParams(params) {
    QueriesEditor(
      modifier = Modifier.padding(8.dp),
      queries = this,
      data = PREVIEW_QUERY_DATA,
      onChange = {},
    )
  }

@Suppress("MagicNumber", "StringLiteralDuplication")
private class QueriesEditorProvider :
  ColoredParameterProvider<ImmutableMap<String, FormulaQuery>>(
    persistentMapOf(),
    persistentMapOf(
      "groceries" to
        FormulaQuery(
          conditions =
            listOf(
              Condition(Category, OneOf, JsonArray(listOf(JsonPrimitive("food")))),
              Condition(Amount, LessThan, JsonPrimitive(-1000)),
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

@Suppress("MagicNumber", "StringLiteralDuplication")
private val PREVIEW_QUERY_DATA =
  QueryEditorData(
    accounts = persistentListOf(QueryEntity("checking", "Checking")),
    categories = persistentListOf(QueryEntity("food", "Food")),
    categoryGroups = persistentListOf(QueryEntity("bills", "Bills")),
    payees = persistentListOf(QueryEntity("employer", "Employer")),
    currentMonth = YearMonth(2025, 6),
    months = YearMonthRange(YearMonth(2024, 1), YearMonth(2025, 6)),
    names =
      persistentMapOf(
        "checking" to "Checking",
        "food" to "Food",
        "bills" to "Bills",
        "employer" to "Employer",
      ),
  )
