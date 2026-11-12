package aktual.budget.rules.components

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.Field
import aktual.budget.rules.domain.ConditionEntities
import aktual.budget.rules.domain.FILTER_FIELDS
import aktual.budget.rules.domain.newFilterCondition
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardColumn
import aktual.core.ui.Chip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

// Whether to match all or any conditions, and the conditions themselves, each edited in a sheet
@Composable
fun ConditionsEditor(
  conditions: ImmutableList<Condition>,
  conditionsOp: ConditionOp,
  data: ConditionEntities,
  onChange: (List<Condition>, ConditionOp) -> Unit,
  modifier: Modifier = Modifier,
  fields: ImmutableList<Field> = FILTER_FIELDS,
) {
  var conditionIndex by rememberSaveable { mutableStateOf<Int?>(null) }

  ConditionsList(
    conditions = conditions,
    conditionsOp = conditionsOp,
    names = data.names,
    onChange = onChange,
    onOpen = { conditionIndex = it },
    modifier = modifier,
    fields = fields,
  )

  conditionIndex?.let { index ->
    ConditionSheetAt(
      index = index,
      conditions = conditions,
      conditionsOp = conditionsOp,
      data = data,
      onChange = onChange,
      onDismiss = { conditionIndex = null },
      fields = fields,
    )
  }
}

// Opens a condition by its index. Adding one opens it too, before it shows up in [conditions]
@Composable
fun ConditionsList(
  conditions: ImmutableList<Condition>,
  conditionsOp: ConditionOp,
  names: ImmutableMap<String, String>,
  onChange: (List<Condition>, ConditionOp) -> Unit,
  onOpen: (Int) -> Unit,
  modifier: Modifier = Modifier,
  fields: ImmutableList<Field> = FILTER_FIELDS,
) =
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(22.dp)) {
    Column {
      SectionLabel(Strings.reportsFormulaQueryMatch)
      AktualSlidingToggleButton(
        modifier = Modifier.fillMaxWidth(),
        selected = if (conditionsOp == Or) Or else And,
        options = ConditionOp.known,
        onSelect = { op -> onChange(conditions, op) },
        string = { op ->
          if (op == Or) Strings.reportsFormulaQueryMatchAny else Strings.reportsFormulaQueryMatchAll
        },
      )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Column {
        SectionLabel(Strings.reportsFormulaQueryConditions)
        if (conditions.isNotEmpty()) {
          CardColumn(modifier = Modifier.fillMaxWidth(), contentPadding = Zero) {
            conditions.fastForEachIndexed { index, condition ->
              if (index > 0) Divider()
              ConditionRow(
                condition = condition,
                names = names,
                onClick = { onOpen(index) },
              )
            }
          }
        }
      }
      AktualButton(
        kind = Tonal,
        modifier = Modifier.fillMaxWidth().testTag(ConditionTags.AddCondition),
        icon = AktualIcons.Add,
        text = Strings.reportsFormulaQueryAddCondition,
        onClick = {
          onOpen(conditions.size)
          val field: Field = if (Payee in fields) Payee else fields.first()
          onChange(conditions + newFilterCondition(field), conditionsOp)
        },
      )
    }
  }

// Shows nothing until the condition exists, e.g. straight after adding it
@Composable
fun ConditionSheetAt(
  index: Int,
  conditions: ImmutableList<Condition>,
  conditionsOp: ConditionOp,
  data: ConditionEntities,
  onChange: (List<Condition>, ConditionOp) -> Unit,
  onDismiss: () -> Unit,
  fields: ImmutableList<Field> = FILTER_FIELDS,
) {
  val condition = conditions.getOrNull(index)
  if (condition != null) {
    ConditionSheet(
      condition = condition,
      data = data,
      fields = fields,
      onChange = { changed ->
        onChange(conditions.toMutableList().apply { set(index, changed) }, conditionsOp)
      },
      onDelete = { onChange(conditions.toMutableList().apply { removeAt(index) }, conditionsOp) },
      onDismiss = onDismiss,
    )
  }
}

@Composable
private fun ConditionRow(
  condition: Condition,
  names: ImmutableMap<String, String>,
  onClick: () -> Unit,
) =
  SettingRow(modifier = Modifier.testTag(ConditionTags.ConditionRow), onClick = onClick) {
    Text(
      modifier = Modifier.width(FIELD_WIDTH),
      text = condition.field.label().replaceFirstChar { it.titlecase() },
      style = typography.bodyMedium,
      color = colors.tableHeaderText,
    )
    Chip(
      text = condition.operator.label(),
      size = Small,
      textStyle = typography.bodySmall,
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

private val FIELD_WIDTH = 72.dp
