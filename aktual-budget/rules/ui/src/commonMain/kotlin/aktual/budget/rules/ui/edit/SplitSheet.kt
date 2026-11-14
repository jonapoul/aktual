package aktual.budget.rules.ui.edit

import aktual.budget.model.RuleAction
import aktual.budget.rules.components.ConditionValue
import aktual.budget.rules.components.QuerySheetPage
import aktual.budget.rules.components.SectionLabel
import aktual.budget.rules.domain.isEditableSplitAmount
import aktual.budget.rules.domain.splits
import aktual.budget.rules.domain.withSplitMethod
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.Chip
import aktual.core.ui.ChipDefaults
import aktual.core.ui.ColorParameters
import aktual.core.ui.Dimens
import aktual.core.ui.HostedSheet
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.slider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

// Shows nothing once the split is gone, e.g. straight after removing it
@Composable
internal fun RuleSplitSheet(
  splitIndex: Int,
  actions: ImmutableList<RuleAction>,
  onAction: OnAction<EditRuleAction>,
  onDismiss: () -> Unit,
) {
  // A focused field commits as the sheet goes, which would land on whatever moved into its index
  var isRemoved by remember { mutableStateOf(false) }
  val amount =
    remember(actions, splitIndex) { actions.splits().getOrNull(splitIndex)?.amount } ?: return
  HostedSheet(onDismiss = onDismiss) {
    QuerySheetPage(
      title = Strings.editRuleSplit(splitIndex),
      close = ::close,
      onDelete = {
        isRemoved = true
        onAction(RemoveSplit(splitIndex))
      },
    ) {
      SplitContent(
        action = amount.value,
        onChange = { if (!isRemoved) onAction(SetAction(amount.index, it)) },
      )
    }
  }
}

@Composable
private fun ColumnScope.SplitContent(action: RuleAction, onChange: (RuleAction) -> Unit) {
  if (!action.isEditableSplitAmount) {
    Text(
      text = Strings.editRuleSplitReadOnly,
      style = typography.bodyMedium,
      color = colors.pageTextSubdued,
    )
    val formula = action.options?.formula
    if (formula != null) {
      Text(text = formula, style = typography.bodyMedium, color = colors.pageText)
    }
  } else {
    Column {
      SectionLabel(Strings.editRuleSplitAllocate)
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
        verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
      ) {
        SPLIT_METHODS.fastForEach { method ->
          Chip(
            text = method.string(),
            colors = ChipDefaults.selectableColors(isSelected = method == action.options?.method),
            role = RadioButton,
            onClick = { onChange(action.withSplitMethod(method)) },
          )
        }
      }
    }

    when (action.options?.method) {
      FixedAmount ->
        Column {
          SectionLabel(Strings.editRuleSplitAmount)
          ConditionValue(
            kind = Money,
            value = JsonPrimitive(action.value?.longOrNull ?: 0L),
            names = persistentMapOf(),
            onChange = { value -> onChange(action.copy(value = value as? JsonPrimitive)) },
            onPickEntity = {},
          )
        }

      FixedPercent ->
        Column {
          SectionLabel(Strings.editRuleSplitPercent)
          PercentSlider(
            percent = action.value?.doubleOrNull ?: 0.0,
            onChange = { percent -> onChange(action.copy(value = JsonPrimitive(percent))) },
          )
        }

      Formula,
      Remainder,
      Unknown,
      null -> Unit
    }
  }
}

// Whole percents only, saved once the thumb is let go so dragging doesn't update the rule each step
@Composable
private fun PercentSlider(
  percent: Double,
  onChange: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  var value by remember(percent) { mutableFloatStateOf(percent.toFloat()) }
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(Dimens.Large),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Slider(
      modifier = Modifier.weight(1f),
      value = value,
      valueRange = 0f..100f,
      onValueChange = { value = it.roundToInt().toFloat() },
      onValueChangeFinished = { onChange(value.roundToLong()) },
      colors = colors.slider(),
    )
    Text(
      modifier = Modifier.widthIn(min = 48.dp),
      text = "${value.roundToInt()}%",
      style = typography.bodyLarge,
      color = colors.pageText,
      textAlign = End,
    )
  }
}

@Composable
private fun RuleAction.Method.string(): String =
  when (this) {
    FixedAmount -> Strings.editRuleSplitFixedAmount
    FixedPercent -> Strings.editRuleSplitFixedPercent
    Remainder -> Strings.editRuleSplitRemainder
    Formula -> Strings.editRuleSplitFormula
    Unknown -> ""
  }

private val SPLIT_METHODS =
  persistentListOf<RuleAction.Method>(FixedAmount, FixedPercent, Remainder)

@Preview
@Composable
private fun PreviewSplitContent(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      SplitContent(
        action =
          RuleAction(
            value = JsonPrimitive(2000),
            op = SetSplitAmount,
            options = RuleAction.Options(method = FixedAmount, splitIndex = 1),
          ),
        onChange = {},
      )
    }
  }

@Preview
@Composable
private fun PreviewPercentSplitContent(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      SplitContent(
        action =
          RuleAction(
            value = JsonPrimitive(25),
            op = SetSplitAmount,
            options = RuleAction.Options(method = FixedPercent, splitIndex = 1),
          ),
        onChange = {},
      )
    }
  }

@Preview
@Composable
private fun PreviewFormulaSplitContent(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      SplitContent(
        action =
          RuleAction(
            value = null,
            op = SetSplitAmount,
            options = RuleAction.Options(method = Formula, formula = "=amount / 2", splitIndex = 1),
          ),
        onChange = {},
      )
    }
  }
