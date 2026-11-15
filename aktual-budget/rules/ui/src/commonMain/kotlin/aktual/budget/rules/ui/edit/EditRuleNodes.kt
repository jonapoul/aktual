package aktual.budget.rules.ui.edit

import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.Field
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleStage
import aktual.budget.model.ScheduleId
import aktual.budget.model.isIdField
import aktual.budget.rules.components.ConditionSheet
import aktual.budget.rules.components.label
import aktual.budget.rules.components.nameOf
import aktual.budget.rules.components.valueLabel
import aktual.budget.rules.domain.ActionSplit
import aktual.budget.rules.domain.ConditionEntities
import aktual.budget.rules.domain.editableKind
import aktual.budget.rules.domain.splits
import aktual.budget.rules.ui.PreviewCondition1
import aktual.budget.rules.ui.PreviewCondition2
import aktual.budget.rules.vm.label as resourceLabel
import aktual.budget.schedules.components.ScheduleStatusBadge
import aktual.budget.schedules.domain.Schedule
import aktual.budget.schedules.domain.amountPrefix
import aktual.core.icons.AktualIcons
import aktual.core.icons.Bolt
import aktual.core.icons.CheveronDown
import aktual.core.icons.Filter
import aktual.core.icons.InformationOutline
import aktual.core.icons.Pencil1
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualIconButton
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.AktualTooltip
import aktual.core.ui.AnimatedFlowRow
import aktual.core.ui.CardColumn
import aktual.core.ui.CardShape
import aktual.core.ui.Chip
import aktual.core.ui.ChipSize.Small
import aktual.core.ui.ColorParameters
import aktual.core.ui.Dimens
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.ShimmerLine
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.stageNode(
  stage: RuleStage,
  isEnabled: Boolean,
  onAction: OnAction<EditRuleAction>,
) =
  item(key = "stage") {
    TimelineRow(
      modifier = Modifier.animateItem(),
      hasRail = true,
      node = { DotNode() },
      isEndOfStep = true,
    ) {
      NodeHeading(Strings.editRuleStage) {
        AktualTooltip(
          modifier = Modifier.size(24.dp),
          imageVector = AktualIcons.InformationOutline,
          contentDescription = null,
          kind = Bare,
          tooltipText = Strings.editRuleStageDesc,
        )
      }

      AktualSlidingToggleButton(
        modifier = Modifier.fillMaxWidth(),
        options = RuleStage.known,
        isEnabled = isEnabled,
        selected = stage,
        onSelect = { onAction(SetStage(it)) },
        string = { stringResource(it.resourceLabel()) },
      )
    }
  }

internal fun LazyListScope.conditionsNode(
  conditions: ImmutableList<Condition>,
  conditionKeys: ImmutableList<Long>,
  conditionsOp: ConditionOp,
  // Null while loading
  names: ImmutableMap<String, String>?,
  isEnabled: Boolean,
  onOpen: ((Long) -> Unit)?,
  onAdd: (() -> Unit)?,
  onAction: OnAction<EditRuleAction>,
) {
  item(key = "when") {
    TimelineRow(
      modifier = Modifier.animateItem(),
      hasRail = true,
      node = { IconNode(AktualIcons.Filter, colors.pillBackground, colors.pillText) },
    ) {
      NodeHeading(Strings.editRuleWhen) {
        ConditionsOpChip(conditionsOp, isEnabled, onSelect = { onAction(SetConditionsOp(it)) })
      }
    }
  }

  itemsIndexed(
    items = conditions,
    key = { index, _ -> "condition-${conditionKeys.getOrNull(index) ?: "at-$index"}" },
  ) { index, condition ->
    TimelineRow(modifier = Modifier.animateItem(), hasRail = true) {
      val key = conditionKeys.getOrNull(index)
      RuleCard(
        title = "${condition.field.label()} ${condition.operator.label()}",
        value =
          condition.valueLabel(names ?: persistentMapOf(), RuleConditions).takeUnless {
            condition.isEmpty()
          },
        hasValue = condition.editableKind(RuleConditions) != NoValue,
        isLoading = names == null && condition.needsNames(),
        isEnabled = isEnabled,
        onClick = clickToOpen(key, onOpen.takeIf { isEnabled }),
      )
    }
  }

  item(key = "add-condition") {
    TimelineRow(modifier = Modifier.animateItem(), hasRail = true, isEndOfStep = true) {
      AddCard(
        text = Strings.reportsFormulaQueryAddCondition,
        isEnabled = isEnabled && onAdd != null,
        onClick = { onAdd?.invoke() },
      )
    }
  }
}

// Edits a copy of the condition, passed to [onDone] when Done is pressed. Without [onDelete] it's a
// new condition, so it can't be deleted
@Composable
internal fun RuleConditionSheet(
  initial: Condition,
  entities: ConditionEntities,
  onDone: (Condition) -> Unit,
  onDelete: (() -> Unit)?,
  onDismiss: () -> Unit,
  canDelete: Boolean = true,
) {
  var draft by remember { mutableStateOf(initial) }
  ConditionSheet(
    condition = draft,
    data = entities,
    schema = RuleConditions,
    onChange = { draft = it },
    onDelete = onDelete?.takeIf { canDelete },
    onDismiss = onDismiss,
    onDone = { onDone(draft) },
    confirm = Update,
    onAdd = { onDone(draft) }.takeIf { onDelete == null },
  )
}

@Composable
private fun ConditionsOpChip(
  op: ConditionOp,
  isEnabled: Boolean,
  onSelect: (ConditionOp) -> Unit,
) {
  var showMenu by remember { mutableStateOf(false) }
  Box {
    Chip(
      modifier = Modifier.alpha(enabledAlpha(isEnabled)),
      text = op.string(),
      size = Small,
      textStyle = typography.bodySmall,
      trailingIcon = AktualIcons.CheveronDown,
      onClick = { showMenu = true }.takeIf { isEnabled },
    )

    AktualDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
      ConditionOp.known.fastForEach { option ->
        AktualDropdownMenuItem(
          text = { Text(option.string()) },
          onClick = {
            showMenu = false
            onSelect(option)
          },
        )
      }
    }
  }
}

@Composable
private fun ConditionOp.string(): String =
  if (this == Or) Strings.editRuleMatchAny else Strings.editRuleMatchAll

internal fun LazyListScope.actionsNode(
  actions: ImmutableList<RuleAction>,
  actionKeys: ImmutableList<Long>,
  // Null while loading
  names: ImmutableMap<String, String>?,
  isEnabled: Boolean,
  onOpen: ((Long) -> Unit)?,
  onAdd: ((splitIndex: Int) -> Unit)?,
  onOpenSplit: (Long) -> Unit,
  onAction: OnAction<EditRuleAction>,
  schedules: ImmutableList<Schedule>? = persistentListOf(),
) {
  item(key = "then") {
    TimelineRow(
      modifier = Modifier.animateItem(),
      hasRail = false,
      node = {
        IconNode(AktualIcons.Bolt, colors.buttonPrimaryBackground, colors.buttonPrimaryText)
      },
    ) {
      NodeHeading(Strings.editRuleThen)
    }
  }

  val splits = actions.splits()
  val isSplit = splits.size > 1

  splits.fastForEach { split ->
    // A split's amount action keeps its key as other splits come and go
    val amountKey = split.amount?.let { actionKeys.getOrNull(it.index) }
    val splitKey = amountKey?.toString() ?: "at-${split.index}"

    if (isSplit) {
      item(key = "split-$splitKey") {
        TimelineRow(modifier = Modifier.animateItem(), hasRail = false) {
          SplitHeader(
            split = split,
            isEnabled = isEnabled,
            onClick = clickToOpen(amountKey, onOpenSplit.takeIf { isEnabled }),
          )
        }
      }
    }

    items(
      items = split.actions,
      key = { (index, _) -> "action-${actionKeys.getOrNull(index) ?: "at-$index"}" },
    ) { (index, action) ->
      TimelineRow(modifier = Modifier.animateItem(), hasRail = false) {
        val key = actionKeys.getOrNull(index)
        if (action.op == LinkSchedule) {
          val id = action.value?.contentOrNull?.let(::ScheduleId)
          ScheduleCard(
            isLoaded = schedules != null,
            isEnabled = isEnabled,
            schedule = id?.let { schedules?.firstOrNull { s -> s.id == it } },
            onOpen = id?.let { { onAction(OpenSchedule(id)) } },
            onEdit = clickToOpen(key, onOpen.takeIf { isEnabled }),
          )
        } else {
          RuleCard(
            title = action.title(),
            value = action.valueText(names ?: persistentMapOf()),
            hasValue = action.op != DeleteTransaction && action.op != Unknown,
            isLoading = names == null && action.needsNames(),
            isEnabled = isEnabled,
            onClick = clickToOpen(key, onOpen.takeIf { isEnabled }),
          )
        }
      }
    }

    if (isSplit) {
      item(key = "add-action-$splitKey") {
        TimelineRow(modifier = Modifier.animateItem(), hasRail = false) {
          AddCard(
            text = Strings.editRuleAddAction,
            isEnabled = isEnabled && onAdd != null,
            onClick = { onAdd?.invoke(split.index) },
          )
        }
      }
    }
  }

  item(key = "add-action") {
    TimelineRow(modifier = Modifier.animateItem(), hasRail = false) {
      Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Large)) {
        if (!isSplit) {
          AddCard(
            modifier = Modifier.weight(1f),
            text = Strings.editRuleAddAction,
            isEnabled = isEnabled && onAdd != null,
            onClick = { onAdd?.invoke(0) },
          )
        }
        if (actions.isNotEmpty()) {
          AddCard(
            modifier = Modifier.weight(1f),
            text = Strings.editRuleAddSplit,
            isEnabled = isEnabled,
            onClick = { onAction(AddSplit) },
          )
        }
      }
    }
  }
}

// Null when there's nothing to open, so the card isn't clickable
private fun clickToOpen(key: Long?, onOpen: ((Long) -> Unit)?): (() -> Unit)? {
  if (key == null || onOpen == null) return null
  return { onOpen(key) }
}

@Composable
private fun SplitHeader(
  split: ActionSplit,
  isEnabled: Boolean,
  onClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .alpha(enabledAlpha(isEnabled))
        .clip(CardShape)
        .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
        .padding(vertical = Dimens.Medium),
    horizontalArrangement = Arrangement.spacedBy(Dimens.Large),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text =
        if (split.index == 0) Strings.editRuleApplyToAll else Strings.editRuleSplit(split.index),
      style = typography.titleSmall,
      color = colors.pageText,
    )
    val summary = split.amount?.value?.splitSummary()
    if (summary != null) {
      Text(
        modifier = Modifier.weight(1f),
        text = summary,
        style = typography.bodyMedium,
        color = colors.pageTextSubdued,
        maxLines = 1,
        overflow = Ellipsis,
      )
    }
  }

@Composable
internal fun RuleAction.splitSummary(): String? =
  when (options?.method) {
    FixedAmount,
    FixedPercent -> splitAmountText()
    Remainder -> Strings.editRuleSplitRemainder
    Formula -> options?.formula ?: Strings.editRuleSplitFormula
    Unknown,
    null -> null
  }

@Composable
private fun RuleCard(
  title: String,
  value: String?,
  hasValue: Boolean,
  isEnabled: Boolean,
  onClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
  isLoading: Boolean = false,
) =
  CardColumn(
    modifier = modifier.fillMaxWidth().alpha(enabledAlpha(isEnabled)),
    verticalArrangement = Arrangement.spacedBy(Dimens.Small),
    onClick = onClick,
  ) {
    Text(
      text = title.uppercase(),
      style = typography.labelSmall,
      color = colors.pageTextSubdued,
      maxLines = 1,
      overflow = Ellipsis,
    )
    if (isLoading) {
      ShimmerValue(fraction = 0.4f)
    } else if (!value.isNullOrEmpty()) {
      Text(
        text = value,
        style = typography.bodyLarge,
        color = colors.pageText,
        maxLines = 2,
        overflow = Ellipsis,
      )
    } else if (hasValue) {
      Text(
        text = Strings.editRuleValueEmpty,
        style = typography.bodyLarge,
        color = colors.pageTextSubdued,
        fontStyle = Italic,
      )
    }
  }

@Composable
private fun ShimmerValue(fraction: Float) =
  ShimmerLine(
    style = typography.bodyLarge,
    modifier = Modifier.fillMaxWidth(fraction).shimmer(rememberShimmer(Window)),
  )

private fun Condition.needsNames(): Boolean =
  !isEmpty() && editableKind(RuleConditions).let { it == Entity || it == EntityList }

private fun RuleAction.needsNames(): Boolean =
  options?.template == null &&
    options?.formula == null &&
    op == RuleAction.Op.Set &&
    field.isIdField() &&
    value?.contentOrNull != null

// Fades things out while the rule is saving, to match the stage toggle and add buttons
@Composable
private fun enabledAlpha(isEnabled: Boolean): Float {
  val alpha by animateFloatAsState(if (isEnabled) 1f else DisabledAlpha)
  return alpha
}

private const val DisabledAlpha = 0.5f

private fun Condition.isEmpty(): Boolean =
  when (val value = value) {
    is JsonArray -> value.isEmpty()
    is JsonPrimitive -> value.contentOrNull.isNullOrEmpty()
    is JsonObject -> false
  }

// Tapping it opens the schedule, and the pencil edits the link. Shows the schedule as upstream's
// rule editor does: its name, payee, account, amount, next date and status. Until the schedules
// have loaded it only shows its title
@Composable
private fun ScheduleCard(
  isLoaded: Boolean,
  isEnabled: Boolean,
  schedule: Schedule?,
  onOpen: (() -> Unit)?,
  onEdit: (() -> Unit)?,
  modifier: Modifier = Modifier,
) =
  CardColumn(
    modifier = modifier.fillMaxWidth().alpha(enabledAlpha(isEnabled)),
    verticalArrangement = Arrangement.spacedBy(Dimens.Small),
    onClick = onOpen.takeIf { schedule != null && isEnabled },
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        modifier = Modifier.weight(1f),
        text = Strings.editRuleOpLinkSchedule.uppercase(),
        style = typography.labelSmall,
        color = colors.pageTextSubdued,
        maxLines = 1,
        overflow = Ellipsis,
      )
      if (schedule != null) ScheduleStatusBadge(schedule.status)
      if (onEdit != null) {
        AktualIconButton(
          modifier = Modifier.size(24.dp),
          imageVector = AktualIcons.Pencil1,
          contentDescription = Strings.editRuleEditScheduleLink,
          onClick = onEdit,
        )
      }
    }

    when {
      !isLoaded -> ShimmerValue(fraction = 0.5f)

      schedule == null ->
        Text(
          text = Strings.editRuleScheduleMissing,
          style = typography.bodyLarge,
          color = colors.pageTextSubdued,
        )

      else -> ScheduleSummary(schedule)
    }
  }

@Composable
private fun ScheduleSummary(schedule: Schedule, modifier: Modifier = Modifier) =
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimens.Small)) {
    Text(
      text = schedule.name ?: Strings.listSchedulesUnnamedSchedule,
      style = typography.bodyLarge,
      color = if (schedule.name == null) colors.pageTextSubdued else colors.pageText,
      maxLines = 2,
      overflow = Ellipsis,
    )
    AnimatedFlowRow {
      item { ScheduleDetail(schedule.payeeName) }
      item { ScheduleDetail(schedule.accountName) }
      item {
        ScheduleDetail(
          text = schedule.amountOp.amountPrefix() + schedule.amount.formattedString(),
          color = if (schedule.amount > Zero) colors.numberPositive else colors.numberNegative,
        )
      }
      item { ScheduleDetail(schedule.nextDate.formatted()) }
    }
  }

@Composable
private fun ScheduleDetail(text: String, color: Color = colors.pageTextSubdued) =
  Text(text = text, style = typography.bodySmall, color = color, maxLines = 1)

@Composable
internal fun RuleAction.title(): String {
  val opText = stringResource(op.resourceLabel())
  val field = field
  return if (op == RuleAction.Op.Set && field != null) "$opText ${field.label()}" else opText
}

@Composable
internal fun RuleAction.valueText(names: ImmutableMap<String, String>): String? {
  val template = options?.template ?: options?.formula
  val value = value
  return when {
    template != null -> template
    op == SetSplitAmount -> splitAmountText()
    op != RuleAction.Op.Set -> value?.contentOrNull
    field.isIdField() -> value?.contentOrNull?.let { names.nameOf(it) }
    field == Field.Amount -> value?.longOrNull?.let { Amount(it).formattedString() }
    field == Cleared ->
      when (value?.booleanOrNull) {
        true -> Strings.reportsFormulaQueryTrue
        false -> Strings.reportsFormulaQueryFalse
        null -> null
      }
    else -> value?.contentOrNull
  }
}

@Composable
@ReadOnlyComposable
internal fun RuleAction.splitAmountText(): String? =
  when (options?.method) {
    FixedAmount -> value?.longOrNull?.let { Amount(it).formattedString() }
    FixedPercent -> value?.contentOrNull?.let { "$it%" }
    Formula,
    Remainder,
    Unknown,
    null -> null
  }

@Preview
@Composable
private fun PreviewStageNode(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    LazyColumn { stageNode(stage = Pre, isEnabled = true, onAction = {}) }
  }

@Preview
@Composable
private fun PreviewConditionsNode(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    LazyColumn {
      conditionsNode(
        conditions = persistentListOf(PreviewCondition1, PreviewCondition2),
        conditionKeys = persistentListOf(0L, 1L),
        conditionsOp = And,
        names = PREVIEW_NAMES,
        isEnabled = true,
        onOpen = {},
        onAdd = {},
        onAction = {},
      )
    }
  }

@Preview
@Composable
private fun PreviewActionsNode(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    LazyColumn {
      actionsNode(
        actions =
          persistentListOf(
            RuleAction(
              value = JsonPrimitive(PREVIEW_CATEGORY),
              op = RuleAction.Op.Set,
              field = Category,
              type = Id,
            ),
            RuleAction(value = JsonPrimitive(-1234), op = RuleAction.Op.Set, field = Field.Amount),
            RuleAction(value = JsonPrimitive("#online"), op = AppendNotes, field = Notes),
            RuleAction(value = null, op = DeleteTransaction),
          ),
        actionKeys = persistentListOf(0L, 1L, 2L, 3L),
        names = PREVIEW_NAMES,
        isEnabled = true,
        onOpen = {},
        onAdd = {},
        onOpenSplit = {},
        onAction = {},
      )
    }
  }

@Preview
@Composable
private fun PreviewSplitActionsNode(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    LazyColumn {
      actionsNode(
        actions =
          persistentListOf(
            RuleAction(value = JsonPrimitive("#online"), op = AppendNotes, field = Notes),
            RuleAction(
              value = JsonPrimitive(2000),
              op = SetSplitAmount,
              options = RuleAction.Options(method = FixedAmount, splitIndex = 1),
            ),
            RuleAction(
              value = JsonPrimitive(PREVIEW_CATEGORY),
              op = RuleAction.Op.Set,
              field = Category,
              type = Id,
              options = RuleAction.Options(splitIndex = 1),
            ),
            RuleAction(
              value = null,
              op = SetSplitAmount,
              options = RuleAction.Options(method = Remainder, splitIndex = 2),
            ),
          ),
        actionKeys = persistentListOf(0L, 1L, 2L, 3L),
        names = PREVIEW_NAMES,
        isEnabled = true,
        onOpen = {},
        onAdd = {},
        onOpenSplit = {},
        onAction = {},
      )
    }
  }

@Preview
@Composable
private fun PreviewEmptyActionsNode(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    LazyColumn {
      actionsNode(
        actions = persistentListOf(),
        actionKeys = persistentListOf(),
        names = PREVIEW_NAMES,
        isEnabled = true,
        onOpen = {},
        onAdd = {},
        onOpenSplit = {},
        onAction = {},
      )
    }
  }

private const val PREVIEW_CATEGORY = "category-id"
private val PREVIEW_NAMES = persistentMapOf(PREVIEW_CATEGORY to "Shopping")
