package aktual.budget.rules.ui.edit

import aktual.budget.model.RuleAction
import aktual.budget.rules.components.ConditionValue
import aktual.budget.rules.components.EntitySheet
import aktual.budget.rules.components.QuerySheetPage
import aktual.budget.rules.components.SectionLabel
import aktual.budget.rules.components.label
import aktual.budget.rules.domain.ACTION_OPS
import aktual.budget.rules.domain.ConditionEntities
import aktual.budget.rules.domain.ConditionEntityLists
import aktual.budget.rules.domain.ConditionValueKind
import aktual.budget.rules.domain.actionFields
import aktual.budget.rules.domain.editableKind
import aktual.budget.rules.domain.entities
import aktual.budget.rules.domain.splitIndex
import aktual.budget.rules.domain.withField
import aktual.budget.rules.domain.withOp
import aktual.budget.rules.vm.label as resourceLabel
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.Chip
import aktual.core.ui.ChipDefaults
import aktual.core.ui.ColorParameters
import aktual.core.ui.Dimens
import aktual.core.ui.HostedSheet
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.stringResource

// Shows nothing until the action at [index] exists, e.g. straight after adding it
@Composable
internal fun RuleActionSheet(
  index: Int,
  actions: ImmutableList<RuleAction>,
  entities: ConditionEntities,
  onAction: OnAction<EditRuleAction>,
  onDismiss: () -> Unit,
) {
  // A focused field commits as the sheet goes, which would land on whatever moved into its index
  var isRemoved by remember { mutableStateOf(false) }
  val action = actions.getOrNull(index) ?: return
  ActionSheet(
    action = action,
    entities = entities,
    onChange = { if (!isRemoved) onAction(SetAction(index, it)) },
    onDelete = {
      isRemoved = true
      onAction(RemoveAction(index))
    },
    onDismiss = onDismiss,
  )
}

// Edits one action in place. Actions the editor can't show can only be deleted
@Composable
private fun ActionSheet(
  action: RuleAction,
  entities: ConditionEntities,
  onChange: (RuleAction) -> Unit,
  onDelete: () -> Unit,
  onDismiss: () -> Unit,
) =
  HostedSheet(onDismiss = onDismiss) {
    var pickingEntity by rememberSaveable { mutableStateOf(false) }
    val kind = action.editableKind()
    val field = action.field
    if (pickingEntity && kind == Entity && field != null) {
      EntitySheet(
        kind = kind,
        value = action.valueElement(kind),
        entities = entities.entities(field),
        onChange = { value -> onChange(action.copy(value = value as? JsonPrimitive)) },
        onDismiss = { pickingEntity = false },
      )
    } else {
      QuerySheetPage(title = Strings.editRuleEditAction, close = ::close, onDelete = onDelete) {
        ActionContent(
          action = action,
          kind = kind,
          names = entities.names,
          onChange = onChange,
          onPickEntity = { pickingEntity = true },
        )
      }
    }
  }

@Composable
private fun ColumnScope.ActionContent(
  action: RuleAction,
  kind: ConditionValueKind?,
  names: ImmutableMap<String, String>,
  onChange: (RuleAction) -> Unit,
  onPickEntity: () -> Unit,
) {
  if (kind == null) {
    val summary = listOfNotNull(action.title(), action.valueText(names)).joinToString(" ")
    Text(
      text = Strings.editRuleUnsupportedAction(summary),
      style = typography.bodyMedium,
      color = colors.pageTextSubdued,
    )
  } else {
    Column {
      SectionLabel(Strings.editRuleActionDo)
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
        verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
      ) {
        ACTION_OPS.fastForEach { op ->
          Chip(
            text = op.string(),
            colors = ChipDefaults.selectableColors(isSelected = op == action.op),
            role = RadioButton,
            onClick = { onChange(action.withOp(op)) },
          )
        }
      }
    }

    val field = action.field
    if (action.op == RuleAction.Op.Set && field != null) {
      Column {
        SectionLabel(Strings.reportsFormulaQueryField)
        AktualExposedDropDownMenu(
          modifier = Modifier.fillMaxWidth(),
          value = field,
          onValueChange = { f -> onChange(action.withField(f)) },
          options = actionFields(action.splitIndex),
          string = { f -> f.label() },
          textStyle = typography.bodyMedium,
        )
      }
    }

    if (kind != NoValue) {
      Column {
        val label =
          if (action.op == RuleAction.Op.Set) {
            Strings.editRuleActionTo
          } else {
            Strings.editRuleActionText
          }
        SectionLabel(label)
        ConditionValue(
          kind = kind,
          value = action.valueElement(kind),
          names = names,
          onChange = { value -> onChange(action.copy(value = value as? JsonPrimitive)) },
          onPickEntity = onPickEntity,
        )
      }
    }
  }
}

// New notes actions have no value yet
private fun RuleAction.valueElement(kind: ConditionValueKind): JsonElement {
  val value = value
  return when {
    value != null && value != JsonNull -> value
    kind == ConditionValueKind.Text -> JsonPrimitive("")
    else -> JsonNull
  }
}

@Composable
private fun RuleAction.Op.string(): String =
  when (this) {
    RuleAction.Op.Set -> Strings.editRuleOpSet
    PrependNotes -> Strings.editRuleOpPrependNotes
    AppendNotes -> Strings.editRuleOpAppendNotes
    DeleteTransaction -> Strings.editRuleOpDeleteTransaction
    SetSplitAmount,
    LinkSchedule,
    Unknown -> stringResource(resourceLabel())
  }

@Preview
@Composable
private fun PreviewActionContent(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      val action =
        RuleAction(
          value = JsonPrimitive("category-id"),
          op = RuleAction.Op.Set,
          field = Category,
          type = Id,
        )
      ActionContent(
        action = action,
        kind = action.editableKind(),
        names = PREVIEW_ENTITIES.names,
        onChange = {},
        onPickEntity = {},
      )
    }
  }

@Preview
@Composable
private fun PreviewNotesActionContent(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      val action = RuleAction(value = JsonPrimitive("#online"), op = AppendNotes, field = Notes)
      ActionContent(
        action = action,
        kind = action.editableKind(),
        names = PREVIEW_ENTITIES.names,
        onChange = {},
        onPickEntity = {},
      )
    }
  }

@Preview
@Composable
private fun PreviewUnsupportedActionContent(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      val action =
        RuleAction(
          value = JsonPrimitive(""),
          op = RuleAction.Op.Set,
          field = Notes,
          options = RuleAction.Options(template = "{{imported_payee}}"),
        )
      ActionContent(
        action = action,
        kind = action.editableKind(),
        names = PREVIEW_ENTITIES.names,
        onChange = {},
        onPickEntity = {},
      )
    }
  }

private val PREVIEW_ENTITIES =
  ConditionEntityLists(
    accounts = persistentListOf(),
    categories = persistentListOf(),
    categoryGroups = persistentListOf(),
    payees = persistentListOf(),
    names = persistentMapOf("category-id" to "Shopping"),
  )
