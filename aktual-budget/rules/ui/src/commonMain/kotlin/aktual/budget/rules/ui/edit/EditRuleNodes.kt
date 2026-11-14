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
import aktual.budget.rules.domain.newActionIndex
import aktual.budget.rules.domain.splits
import aktual.budget.rules.ui.LocalNameFetcher
import aktual.budget.rules.ui.PreviewCondition1
import aktual.budget.rules.ui.PreviewCondition2
import aktual.budget.rules.vm.label as resourceLabel
import aktual.core.icons.AktualIcons
import aktual.core.icons.Bolt
import aktual.core.icons.CheveronDown
import aktual.core.icons.Filter
import aktual.core.icons.InformationOutline
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.AktualTooltip
import aktual.core.ui.CardColumn
import aktual.core.ui.CardShape
import aktual.core.ui.Chip
import aktual.core.ui.ChipSize.Small
import aktual.core.ui.ColorParameters
import aktual.core.ui.Dimens
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.formattedString
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.flowOf
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
    TimelineRow(hasRail = true, node = { DotNode() }, isEndOfStep = true) {
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
  names: ImmutableMap<String, String>,
  isEnabled: Boolean,
  onOpen: ((Int) -> Unit)?,
  onAction: OnAction<EditRuleAction>,
) {
  item(key = "when") {
    TimelineRow(
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
      RuleCard(
        title = "${condition.field.label()} ${condition.operator.label()}",
        value = condition.valueLabel(names, RuleConditions),
        onClick = onOpen?.let { open -> { open(index) } }?.takeIf { isEnabled },
      )
    }
  }

  item(key = "add-condition") {
    TimelineRow(modifier = Modifier.animateItem(), hasRail = true, isEndOfStep = true) {
      AddCard(
        text = Strings.reportsFormulaQueryAddCondition,
        isEnabled = isEnabled,
        onClick = {
          onOpen?.invoke(conditions.size)
          onAction(AddCondition)
        },
      )
    }
  }
}

// Shows nothing until the condition at [index] exists, e.g. straight after adding it
@Composable
internal fun RuleConditionSheet(
  index: Int,
  conditions: ImmutableList<Condition>,
  entities: ConditionEntities,
  onAction: OnAction<EditRuleAction>,
  onDismiss: () -> Unit,
) {
  val condition = conditions.getOrNull(index) ?: return
  ConditionSheet(
    condition = condition,
    data = entities,
    schema = RuleConditions,
    onChange = { onAction(SetCondition(index, it)) },
    onDelete = { onAction(RemoveCondition(index)) },
    onDismiss = onDismiss,
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
  names: ImmutableMap<String, String>,
  isEnabled: Boolean,
  onOpen: ((Int) -> Unit)?,
  onOpenSplit: (Int) -> Unit,
  onAction: OnAction<EditRuleAction>,
  hasNext: Boolean = false,
) {
  item(key = "then") {
    TimelineRow(
      hasRail = hasNext,
      node = {
        IconNode(AktualIcons.Bolt, colors.buttonPrimaryBackground, colors.buttonPrimaryText)
      },
    ) {
      NodeHeading(Strings.editRuleThen)
    }
  }

  val splits = actions.splits()
  val isSplit = splits.size > 1
  fun addAction(splitIndex: Int) {
    onOpen?.invoke(actions.newActionIndex(splitIndex))
    onAction(AddAction(splitIndex))
  }

  splits.fastForEach { split ->
    // A split's amount action keeps its key as other splits come and go
    val splitKey =
      split.amount?.let { actionKeys.getOrNull(it.index) }?.toString() ?: "at-${split.index}"

    if (isSplit) {
      item(key = "split-$splitKey") {
        TimelineRow(modifier = Modifier.animateItem(), hasRail = hasNext) {
          SplitHeader(
            split = split,
            onClick = { onOpenSplit(split.index) }.takeIf { isEnabled && split.amount != null },
          )
        }
      }
    }

    items(
      items = split.actions,
      key = { (index, _) -> "action-${actionKeys.getOrNull(index) ?: "at-$index"}" },
    ) { (index, action) ->
      TimelineRow(modifier = Modifier.animateItem(), hasRail = hasNext) {
        RuleCard(
          title = action.title(),
          value = action.valueText(names),
          onClick = onOpen?.let { open -> { open(index) } }?.takeIf { isEnabled },
        )
      }
    }

    if (isSplit) {
      item(key = "add-action-$splitKey") {
        TimelineRow(modifier = Modifier.animateItem(), hasRail = hasNext) {
          AddCard(
            text = Strings.editRuleAddAction,
            isEnabled = isEnabled,
            onClick = { addAction(split.index) },
          )
        }
      }
    }
  }

  item(key = "add-action") {
    TimelineRow(modifier = Modifier.animateItem(), hasRail = hasNext, isEndOfStep = hasNext) {
      Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Large)) {
        if (!isSplit) {
          AddCard(
            modifier = Modifier.weight(1f),
            text = Strings.editRuleAddAction,
            isEnabled = isEnabled,
            onClick = { addAction(0) },
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

@Composable
private fun SplitHeader(split: ActionSplit, onClick: (() -> Unit)?, modifier: Modifier = Modifier) =
  Row(
    modifier =
      modifier
        .fillMaxWidth()
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
  onClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
) =
  CardColumn(
    modifier = modifier.fillMaxWidth(),
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
    if (!value.isNullOrEmpty()) {
      Text(
        text = value,
        style = typography.bodyLarge,
        color = colors.pageText,
        maxLines = 2,
        overflow = Ellipsis,
      )
    }
  }

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
    op == LinkSchedule -> scheduleName(value?.contentOrNull)
    op == SetSplitAmount -> splitAmountText()
    op != RuleAction.Op.Set -> value?.contentOrNull
    field.isIdField() ->
      value?.contentOrNull?.let { names.nameOf(it) } ?: Strings.reportsFormulaQueryNothing
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

@Composable
private fun scheduleName(id: String?): String? {
  val nameFetcher = LocalNameFetcher.current
  val flow =
    remember(nameFetcher, id) { id?.let { nameFetcher.name(ScheduleId(it)) } ?: flowOf(null) }
  val name by flow.collectAsStateWithLifecycle(initialValue = null)
  return name
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
        onOpenSplit = {},
        onAction = {},
      )
    }
  }

private const val PREVIEW_CATEGORY = "category-id"
private val PREVIEW_NAMES = persistentMapOf(PREVIEW_CATEGORY to "Shopping")
