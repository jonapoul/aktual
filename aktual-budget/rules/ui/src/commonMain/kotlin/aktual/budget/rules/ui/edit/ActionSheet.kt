package aktual.budget.rules.ui.edit

import aktual.budget.model.RuleAction
import aktual.budget.model.ScheduleId
import aktual.budget.rules.components.ConditionValue
import aktual.budget.rules.components.EntitySheet
import aktual.budget.rules.components.QuerySheetPage
import aktual.budget.rules.components.label
import aktual.budget.rules.domain.ConditionEntities
import aktual.budget.rules.domain.ConditionEntityLists
import aktual.budget.rules.domain.ConditionValueKind
import aktual.budget.rules.domain.actionFields
import aktual.budget.rules.domain.actionOps
import aktual.budget.rules.domain.editableKind
import aktual.budget.rules.domain.entities
import aktual.budget.rules.domain.splitIndex
import aktual.budget.rules.domain.withField
import aktual.budget.rules.domain.withOp
import aktual.budget.rules.vm.label as resourceLabel
import aktual.budget.schedules.components.SchedulePicker
import aktual.budget.schedules.domain.Schedule
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.AnimatedFlowRow
import aktual.core.ui.Chip
import aktual.core.ui.ChipDefaults
import aktual.core.ui.ColorParameters
import aktual.core.ui.HostedSheet
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.SectionLabel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.jetbrains.compose.resources.stringResource

// Edits a copy of the action, passed to [onDone] when Done is pressed. Without [onDelete] it's a
// new action, so it can't be deleted
@Composable
internal fun RuleActionSheet(
  initial: RuleAction,
  entities: ConditionEntities,
  schedules: ImmutableList<Schedule>?,
  onDone: (RuleAction) -> Unit,
  onDelete: (() -> Unit)?,
  onDismiss: () -> Unit,
) {
  var draft by remember { mutableStateOf(initial) }
  ActionSheet(
    action = draft,
    entities = entities,
    schedules = schedules,
    onChange = { draft = it },
    onDone = { onDone(draft) },
    onDelete = onDelete,
    onDismiss = onDismiss,
  )
}

// Actions the editor can't show can only be deleted
@Composable
private fun ActionSheet(
  action: RuleAction,
  entities: ConditionEntities,
  schedules: ImmutableList<Schedule>?,
  onChange: (RuleAction) -> Unit,
  onDone: () -> Unit,
  onDelete: (() -> Unit)?,
  onDismiss: () -> Unit,
) =
  HostedSheet(onDismiss = onDismiss) {
    var pickingEntity by rememberSaveable { mutableStateOf(false) }
    var pickingSchedule by rememberSaveable { mutableStateOf(false) }
    val kind = action.editableKind()
    val field = action.field
    val isLink = action.op == LinkSchedule
    if (pickingSchedule && isLink && schedules != null) {
      SchedulePickerPage(
        selected = action.scheduleId,
        schedules = schedules,
        onSelect = { id -> onChange(action.copy(value = JsonPrimitive(id.value))) },
        onDismiss = { pickingSchedule = false },
      )
    } else if (pickingEntity && kind == Entity && field != null) {
      EntitySheet(
        kind = kind,
        value = action.valueElement(kind),
        entities = entities.entities(field),
        onChange = { value -> onChange(action.copy(value = value as? JsonPrimitive)) },
        onDismiss = { pickingEntity = false },
      )
    } else {
      QuerySheetPage(
        title = if (onDelete == null) Strings.editRuleAddAction else Strings.editRuleEditAction,
        close = ::close,
        onDelete = onDelete,
        onDone = onDone,
        confirm = if (onDelete == null) Add else Update,
        isDoneEnabled = !isLink || action.scheduleId != null,
      ) {
        ActionContent(
          action = action,
          kind = kind,
          names = entities.names,
          schedules = schedules,
          onChange = onChange,
          onPickEntity = { pickingEntity = true },
          onPickSchedule = { pickingSchedule = true },
        )
      }
    }
  }

@Composable
private fun ColumnScope.ActionContent(
  action: RuleAction,
  kind: ConditionValueKind?,
  names: ImmutableMap<String, String>,
  schedules: ImmutableList<Schedule>?,
  onChange: (RuleAction) -> Unit,
  onPickEntity: () -> Unit,
  onPickSchedule: () -> Unit,
) {
  val isLink = action.op == LinkSchedule
  if (kind == null && !isLink) {
    val summary = listOfNotNull(action.title(), action.valueText(names)).joinToString(" ")
    Text(
      text = Strings.editRuleUnsupportedAction(summary),
      style = typography.bodyMedium,
      color = colors.pageTextSubdued,
    )
  } else {
    Column {
      SectionLabel(Strings.editRuleActionDo)
      AnimatedFlowRow {
        actionOps(action.splitIndex).fastForEach { op ->
          item(key = op) {
            Chip(
              text = op.string(),
              colors = ChipDefaults.selectableColors(isSelected = op == action.op),
              role = RadioButton,
              onClick = { onChange(action.withOp(op)) },
            )
          }
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

    if (isLink) {
      Column {
        SectionLabel(Strings.editRuleActionSchedule)
        val id = action.scheduleId
        val schedule = id?.let { schedules?.firstOrNull { s -> s.id == it } }
        AktualButton(
          modifier = Modifier.fillMaxWidth(),
          text =
            when {
              schedules == null -> Strings.editRuleSchedulesLoading
              id == null -> Strings.editRulePickSchedule
              schedule == null -> Strings.editRuleScheduleMissing
              else -> schedule.name ?: Strings.listSchedulesUnnamedSchedule
            },
          isEnabled = schedules != null,
          onClick = onPickSchedule,
        )
      }
    } else if (kind != null && kind != NoValue) {
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

// Completed schedules are only offered behind their status chip, unless already linked
@Composable
private fun SchedulePickerPage(
  selected: ScheduleId?,
  schedules: ImmutableList<Schedule>,
  onSelect: (ScheduleId) -> Unit,
  onDismiss: () -> Unit,
) =
  HostedSheet(onDismiss = onDismiss) {
    val (options, completed) =
      remember(schedules, selected) {
        val (open, done) = schedules.partition { !it.isCompleted || it.id == selected }
        open.toImmutableList() to done.toImmutableList()
      }
    SchedulePicker(
      selected = selected,
      options = options,
      completed = completed,
      onSelect = { id -> close { onSelect(id) } },
      onUnlink = null,
    )
  }

private val RuleAction.scheduleId: ScheduleId?
  get() = value?.contentOrNull?.takeIf { it.isNotEmpty() }?.let(::ScheduleId)

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
    LinkSchedule -> Strings.editRuleOpLinkSchedule
    SetSplitAmount,
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
        schedules = persistentListOf(),
        onChange = {},
        onPickEntity = {},
        onPickSchedule = {},
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
        schedules = persistentListOf(),
        onChange = {},
        onPickEntity = {},
        onPickSchedule = {},
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
        schedules = persistentListOf(),
        onChange = {},
        onPickEntity = {},
        onPickSchedule = {},
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

@Preview
@Composable
private fun PreviewLinkScheduleActionContent(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      val action = RuleAction(value = null, op = LinkSchedule)
      ActionContent(
        action = action,
        kind = action.editableKind(),
        names = PREVIEW_ENTITIES.names,
        schedules = persistentListOf(),
        onChange = {},
        onPickEntity = {},
        onPickSchedule = {},
      )
    }
  }
