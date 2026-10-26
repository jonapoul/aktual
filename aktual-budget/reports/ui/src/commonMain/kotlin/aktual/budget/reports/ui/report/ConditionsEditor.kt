package aktual.budget.reports.ui.report

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.Field
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.vm.QUERY_FIELDS
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.newQueryCondition
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.Chip
import aktual.core.ui.TonalButton
import androidx.compose.foundation.background
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
internal fun ConditionsEditor(
  conditions: ImmutableList<Condition>,
  conditionsOp: ConditionOp,
  data: QueryEditorData,
  onChange: (List<Condition>, ConditionOp) -> Unit,
  modifier: Modifier = Modifier,
  fields: ImmutableList<Field> = QUERY_FIELDS,
) {
  var conditionIndex by rememberSaveable { mutableStateOf<Int?>(null) }

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
          Column(modifier = Modifier.fillMaxWidth().background(colors.tableBackground, CardShape)) {
            conditions.fastForEachIndexed { index, condition ->
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
        icon = AktualIcons.Add,
        text = Strings.reportsFormulaQueryAddCondition,
        onClick = {
          conditionIndex = conditions.size
          val field: Field = if (Payee in fields) Payee else fields.first()
          onChange(conditions + newQueryCondition(field), conditionsOp)
        },
      )
    }
  }

  val index = conditionIndex
  val condition = index?.let(conditions::getOrNull)
  if (index != null && condition != null) {
    ConditionSheet(
      condition = condition,
      data = data,
      fields = fields,
      onChange = { changed ->
        onChange(conditions.toMutableList().apply { set(index, changed) }, conditionsOp)
      },
      onDelete = { onChange(conditions.toMutableList().apply { removeAt(index) }, conditionsOp) },
      onDismiss = { conditionIndex = null },
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
