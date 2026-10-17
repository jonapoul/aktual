package aktual.budget.reports.ui.report

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.dashboard.NameDialog
import aktual.budget.reports.vm.FormulaQuery
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.TimeFrame
import aktual.budget.reports.vm.newQueryCondition
import aktual.core.icons.material.Add
import aktual.core.icons.material.CalendarToday
import aktual.core.icons.material.Delete
import aktual.core.icons.material.Edit
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BackHandler
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.IconButtonColorProvider
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.TonalButton
import aktual.core.ui.bareIconButton
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily.Companion.Monospace
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf

// Edits one query of the draft in place, so back keeps the changes until the report is saved
@Composable
internal fun QueryScreen(
  name: String,
  query: FormulaQuery,
  data: QueryEditorData?,
  isNameFree: (String) -> Boolean,
  onChange: (FormulaQuery) -> Unit,
  onRename: (String) -> Unit,
  onDelete: () -> Unit,
  onClose: () -> Unit,
) {
  var dialog by rememberSaveable { mutableStateOf<QueryDialog?>(null) }
  BackHandler(onBack = onClose)
  QueryScaffold(
    name = name,
    query = query,
    data = data,
    onChange = onChange,
    onRename = { dialog = Rename },
    onDelete = { dialog = Delete },
    onBack = onClose,
  )

  when (dialog) {
    Rename -> {
      NameDialog(
        title = Strings.reportsFormulaQueryRename,
        placeholder = Strings.reportsFormulaQueryName,
        confirmText = Strings.reportsFormulaQueryRenameConfirm,
        initialName = name,
        capitalization = None,
        isValid = { it == name || isNameFree(it) },
        onDismiss = { dialog = null },
        onConfirm = { newName ->
          dialog = null
          if (newName != name) onRename(newName)
        },
      )
    }
    Delete -> {
      DeleteQueryDialog(
        name = name,
        onConfirm = {
          dialog = null
          onDelete()
        },
        onDismiss = { dialog = null },
      )
    }
    null -> {}
  }
}

private enum class QueryDialog {
  Rename,
  Delete,
}

@Composable
private fun DeleteQueryDialog(name: String, onConfirm: () -> Unit, onDismiss: () -> Unit) =
  AktualAlertDialog(
    title = Strings.reportsFormulaQueryDeleteTitle(name),
    highlight = colors.errorText,
    onDismissRequest = onDismiss,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.reportsFormulaQueryCancel) }
      TextButton(modifier = Modifier.testTag(Tags.ConfirmDeleteQuery), onClick = onConfirm) {
        Text(Strings.reportsFormulaQueryDeleteConditionConfirm, color = colors.errorText)
      }
    },
    content = {},
  )

private val DELETE_COLORS = IconButtonColorProvider { colors, isPressed ->
  colors.bareIconButton(isPressed).copy(contentColor = colors.errorText)
}

@Composable
internal fun QueryScaffold(
  name: String,
  query: FormulaQuery,
  data: QueryEditorData?,
  onChange: (FormulaQuery) -> Unit,
  onRename: () -> Unit,
  onDelete: () -> Unit,
  onBack: () -> Unit,
) =
  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = onBack) },
        title = { Text(text = name, fontFamily = Monospace) },
        actions = {
          BareIconButton(
            modifier = Modifier.testTag(Tags.RenameQuery),
            imageVector = MaterialIcons.Edit,
            contentDescription = Strings.reportsFormulaQueryRename,
            onClick = onRename,
          )
          BareIconButton(
            modifier = Modifier.padding(end = 4.dp).testTag(Tags.DeleteQuery),
            imageVector = MaterialIcons.Delete,
            contentDescription = Strings.reportsFormulaQueryDelete,
            colors = DELETE_COLORS,
            onClick = onDelete,
          )
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()
      if (data == null) {
        CircularProgressIndicator(modifier = Modifier.align(Center), color = colors.pageText)
      } else {
        QueryContent(
          modifier = Modifier.padding(innerPadding),
          query = query,
          data = data,
          onChange = onChange,
        )
      }
    }
  }

@Composable
private fun QueryContent(
  query: FormulaQuery,
  data: QueryEditorData,
  onChange: (FormulaQuery) -> Unit,
  modifier: Modifier = Modifier,
) {
  var conditionIndex by rememberSaveable { mutableStateOf<Int?>(null) }
  var showTimeFrame by rememberSaveable { mutableStateOf(false) }

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScrollWithBar(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 4.dp),
    verticalArrangement = Arrangement.spacedBy(22.dp),
  ) {
    Column {
      SectionLabel(Strings.reportsFormulaQueryMatch)
      AktualSlidingToggleButton(
        modifier = Modifier.fillMaxWidth(),
        selected = if (query.conditionsOp == Or) Or else And,
        options = CONDITION_OPS,
        onSelect = { op -> onChange(query.copy(conditionsOp = op)) },
        string = { op ->
          if (op == Or) Strings.reportsFormulaQueryMatchAny else Strings.reportsFormulaQueryMatchAll
        },
      )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Column {
        SectionLabel(Strings.reportsFormulaQueryConditions)
        if (query.conditions.isNotEmpty()) {
          Column(
            modifier = Modifier.fillMaxWidth().background(colors.tableBackground, CardShape),
          ) {
            query.conditions.fastForEachIndexed { index, condition ->
              if (index > 0) Divider()
              ConditionRow(
                condition = condition,
                names = data.names,
                onClick = { conditionIndex = index },
              )
            }
          }
        }
      }
      TonalButton(
        modifier = Modifier.fillMaxWidth().testTag(Tags.AddCondition),
        icon = MaterialIcons.Add,
        text = Strings.reportsFormulaQueryAddCondition,
        onClick = {
          conditionIndex = query.conditions.size
          onChange(query.copy(conditions = query.conditions + newQueryCondition(Payee)))
        },
      )
    }

    Column {
      SectionLabel(Strings.reportsFormulaQueryTimeFrame)
      TimeFrameRow(timeFrame = query.timeFrame, onClick = { showTimeFrame = true })
    }

    BottomSpacing()
  }

  val index = conditionIndex
  val condition = index?.let(query.conditions::getOrNull)
  if (index != null && condition != null) {
    ConditionSheet(
      condition = condition,
      data = data,
      onChange = { changed ->
        val conditions = query.conditions.toMutableList().apply { set(index, changed) }
        onChange(query.copy(conditions = conditions))
      },
      onDelete = {
        val conditions = query.conditions.toMutableList().apply { removeAt(index) }
        onChange(query.copy(conditions = conditions))
      },
      onDismiss = { conditionIndex = null },
    )
  }

  if (showTimeFrame) {
    TimeFrameSheet(
      timeFrame = query.timeFrame,
      data = data,
      onChange = { timeFrame -> onChange(query.copy(timeFrame = timeFrame)) },
      onDismiss = { showTimeFrame = false },
    )
  }
}

@Composable
private fun ConditionRow(
  condition: Condition,
  names: ImmutableMap<String, String>,
  onClick: () -> Unit,
) =
  SettingRow(modifier = Modifier.testTag(Tags.ConditionRow), onClick = onClick) {
    Text(
      modifier = Modifier.width(FIELD_WIDTH),
      text = condition.field.label().replaceFirstChar { it.titlecase() },
      style = typography.bodyMedium,
      color = colors.tableHeaderText,
    )
    Text(
      modifier =
        Modifier.background(colors.pillBackground, OPERATOR_SHAPE)
          .padding(horizontal = 8.dp, vertical = 2.dp),
      text = condition.operator.label(),
      style = typography.bodySmall,
      color = colors.pillText,
    )
    Text(
      modifier = Modifier.weight(1f),
      text = condition.valueLabel(names).orEmpty(),
      style = typography.bodyMedium,
      color = colors.tableText,
      maxLines = 1,
      overflow = Ellipsis,
    )
  }

@Composable
private fun TimeFrameRow(timeFrame: TimeFrame?, onClick: () -> Unit) =
  Box(modifier = Modifier.clip(CardShape).background(colors.tableBackground)) {
    SettingRow(modifier = Modifier.testTag(Tags.TimeFrameRow), onClick = onClick) {
      Icon(
        modifier = Modifier.size(20.dp),
        imageVector = MaterialIcons.CalendarToday,
        contentDescription = null,
        tint = colors.tableHeaderText,
      )
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = (timeFrame?.mode ?: Full).label(),
          style = typography.bodyLarge,
          color = colors.tableText,
        )
        if (timeFrame != null && timeFrame.hasMonths()) {
          Text(
            text = timeFrame.label(),
            style = typography.bodySmall,
            color = colors.tableHeaderText,
          )
        }
      }
    }
  }

internal val CONDITION_OPS = persistentListOf(ConditionOp.And, ConditionOp.Or)
private val FIELD_WIDTH = 72.dp
private val OPERATOR_SHAPE = RoundedCornerShape(6.dp)

@PortraitPreview
@Composable
private fun PreviewQueryScaffold(
  @PreviewParameter(QueryProvider::class) params: ColoredParams<FormulaQuery>,
) =
  PreviewWithColoredParams(params) {
    QueryScaffold(
      name = "groceries",
      query = this,
      data = PREVIEW_QUERY_DATA,
      onChange = {},
      onRename = {},
      onDelete = {},
      onBack = {},
    )
  }

private class QueryProvider :
  ColoredParameterProvider<FormulaQuery>(
    PREVIEW_FORMULA_META.queries.getValue("groceries"),
    PREVIEW_FORMULA_META.queries.getValue("income"),
    FormulaQuery(conditions = emptyList(), conditionsOp = And, timeFrame = null),
  )
