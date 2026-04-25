package aktual.budget.rules.ui.edit

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.Field
import aktual.budget.rules.ui.PreviewRule1
import aktual.budget.rules.ui.displayString
import aktual.budget.rules.ui.filteredOperators
import aktual.budget.rules.ui.string
import aktual.budget.rules.vm.Rule
import aktual.core.icons.material.Add
import aktual.core.icons.material.Delete
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.CardShape
import aktual.core.ui.ExposedDropDownMenu
import aktual.core.ui.IconButtonColorProvider
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.PrimaryIconButton
import aktual.core.ui.ThemeParameters
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun Conditions(
  rule: Rule,
  isEnabled: Boolean,
  onAction: EditRuleActionHandler,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier =
      modifier.fillMaxWidth().background(theme.cardBackground, CardShape).padding(CARD_PADDING),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Row(verticalAlignment = Alignment.Top) {
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = Strings.editRuleConditions, style = AktualTypography.titleSmall)

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(text = Strings.editRuleConditionsDescPrefix, style = AktualTypography.bodyLarge)

          ExposedDropDownMenu(
            value = rule.conditionsOp,
            onValueChange = { op -> onAction(SetConditionOp(op)) },
            options = ConditionOp.entries.toImmutableList(),
            string = { op -> op.string() },
            textStyle = AktualTypography.bodySmall,
            isEnabled = isEnabled,
            contentPadding = BUTTON_PADDING,
          )

          Text(text = Strings.editRuleConditionsDescSuffix, style = AktualTypography.bodyLarge)
        }
      }
      PrimaryIconButton(
        enabled = isEnabled,
        imageVector = MaterialIcons.Add,
        contentDescription = Strings.editRuleCreate,
        onClick = { onAction(AppendCondition) },
      )
    }

    rule.conditions.fastForEachIndexed { index, condition ->
      Condition(condition, index, isEnabled, onAction)
    }
  }
}

@Composable
private fun Condition(
  condition: Condition,
  index: Int,
  isEnabled: Boolean,
  onAction: EditRuleActionHandler,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier.fillMaxWidth().background(theme.pillBackground, CardShape).padding(4.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      ExposedDropDownMenu(
        modifier = Modifier.weight(1f),
        value = condition.field,
        onValueChange = { f -> onAction(SetConditionField(f, index)) },
        options = VISIBLE_FIELDS,
        string = { f -> f.string(condition.options) },
        textStyle = AktualTypography.bodySmall,
        isEnabled = isEnabled,
        contentPadding = BUTTON_PADDING,
      )

      NormalIconButton(
        imageVector = MaterialIcons.Delete,
        contentDescription = Strings.rulesItemDelete,
        onClick = { onAction(DeleteCondition(index)) },
        colors = IconButtonColorProvider.NormalRed,
        isEnabled = isEnabled,
      )
    }

    ExposedDropDownMenu(
      modifier = Modifier.fillMaxWidth(),
      value = condition.operator,
      onValueChange = { op -> onAction(SetConditionOperator(op, index)) },
      options = filteredOperators(condition),
      string = { f -> f.displayString() },
      textStyle = AktualTypography.bodySmall,
      isEnabled = isEnabled,
      contentPadding = BUTTON_PADDING,
    )

    ConditionValueEditor(
      modifier = Modifier.fillMaxWidth(),
      field = condition.field,
      value = condition.value,
      index = index,
      isEnabled = isEnabled,
      onAction = onAction,
    )
  }
}

private val DUPE_FIELDS = setOf(Field.Acct, Field.Description, Field.ImportedDescription)

private val VISIBLE_FIELDS = Field.entries.minus(DUPE_FIELDS).toImmutableList()

@Preview
@Composable
private fun PreviewConditions(@PreviewParameter(ThemeParameters::class) theme: Theme) {
  PreviewWithTheme(theme) { Conditions(rule = PreviewRule1, isEnabled = true, onAction = {}) }
}
