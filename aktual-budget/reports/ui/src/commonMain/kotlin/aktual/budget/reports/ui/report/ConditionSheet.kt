package aktual.budget.reports.ui.report

import aktual.budget.model.Condition
import aktual.budget.reports.vm.QUERY_FIELDS
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.editableKind
import aktual.budget.reports.vm.entities
import aktual.budget.reports.vm.newQueryCondition
import aktual.budget.reports.vm.queryOperators
import aktual.budget.reports.vm.withOperator
import aktual.core.l10n.Strings
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Edits one condition of a query in place. Conditions the editor can't show can only be deleted
@Composable
internal fun ConditionSheet(
  condition: Condition,
  data: QueryEditorData,
  onChange: (Condition) -> Unit,
  onDelete: () -> Unit,
  onDismiss: () -> Unit,
) =
  QuerySheet(
    title = Strings.reportsFormulaQueryEditCondition,
    onDismiss = onDismiss,
    onDelete = onDelete,
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
            onValueChange = { field -> onChange(newQueryCondition(field)) },
            options = QUERY_FIELDS,
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
            options = queryOperators(condition.field),
            string = { op -> op.label() },
            textStyle = typography.bodyMedium,
          )
        }
      }

      ConditionValue(
        kind = kind,
        value = condition.value,
        entities = data.entities(condition.field),
        names = data.names,
        onChange = { value -> onChange(condition.copy(value = value)) },
      )
    }
  }
