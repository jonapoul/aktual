package aktual.budget.rules.components

import aktual.budget.model.Condition
import aktual.budget.model.Field
import aktual.budget.rules.domain.ConditionEntities
import aktual.budget.rules.domain.FILTER_FIELDS
import aktual.budget.rules.domain.editableKind
import aktual.budget.rules.domain.entities
import aktual.budget.rules.domain.filterOperators
import aktual.budget.rules.domain.newFilterCondition
import aktual.budget.rules.domain.withOperator
import aktual.core.l10n.Strings
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.HostedSheet
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

// Edits one condition of a query in place. Conditions the editor can't show can only be deleted
@Composable
fun ConditionSheet(
  condition: Condition,
  data: ConditionEntities,
  onChange: (Condition) -> Unit,
  onDelete: () -> Unit,
  onDismiss: () -> Unit,
  fields: ImmutableList<Field> = FILTER_FIELDS,
) =
  HostedSheet(onDismiss = onDismiss) {
    var pickingEntity by rememberSaveable { mutableStateOf(false) }
    val kind = condition.editableKind()
    if (pickingEntity && (kind == Entity || kind == EntityList)) {
      EntitySheet(
        kind = kind,
        value = condition.value,
        entities = data.entities(condition.field),
        onChange = { value -> onChange(condition.copy(value = value)) },
        onDismiss = { pickingEntity = false },
      )
    } else {
      QuerySheetPage(
        title = Strings.reportsFormulaQueryEditCondition,
        close = ::close,
        onDelete = onDelete,
      ) {
        ConditionContent(
          condition = condition,
          fields = fields,
          data = data,
          onChange = onChange,
          onPickEntity = { pickingEntity = true },
        )
      }
    }
  }

@Composable
private fun ConditionContent(
  condition: Condition,
  fields: ImmutableList<Field>,
  data: ConditionEntities,
  onChange: (Condition) -> Unit,
  onPickEntity: () -> Unit,
) {
  val kind = condition.editableKind()
  if (kind == null) {
    val summary = "${condition.field.label()} ${condition.operator.label()} ${condition.value}"
    Text(
      text = Strings.reportsFormulaQueryUnsupportedCondition(summary),
      style = typography.bodyMedium,
      color = colors.pageTextSubdued,
    )
  } else {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      Column(modifier = Modifier.weight(1f)) {
        SectionLabel(Strings.reportsFormulaQueryField)
        AktualExposedDropDownMenu(
          modifier = Modifier.fillMaxWidth(),
          value = condition.field,
          onValueChange = { field -> onChange(newFilterCondition(field)) },
          options = fields,
          string = { field -> field.label() },
          textStyle = typography.bodyMedium,
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        SectionLabel(Strings.reportsFormulaQueryOperator)
        AktualExposedDropDownMenu(
          modifier = Modifier.fillMaxWidth(),
          value = condition.operator,
          onValueChange = { op -> onChange(condition.withOperator(op)) },
          options = filterOperators(condition.field),
          string = { op -> op.label() },
          textStyle = typography.bodyMedium,
        )
      }
    }

    ConditionValue(
      kind = kind,
      value = condition.value,
      names = data.names,
      onChange = { value -> onChange(condition.copy(value = value)) },
      onPickEntity = onPickEntity,
    )
  }
}
