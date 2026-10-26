package aktual.budget.rules.ui.list

import aktual.budget.model.Condition
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleStage
import aktual.budget.rules.ui.PreviewRule1
import aktual.budget.rules.ui.PreviewRule2
import aktual.budget.rules.vm.Rule
import aktual.core.icons.AktualIcons
import aktual.core.icons.Trash
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.Chip
import aktual.core.ui.ChipColors
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.ColorParameters
import aktual.core.ui.ColorParams
import aktual.core.ui.Dimens
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.RowShape
import aktual.core.ui.SwipeAction
import aktual.core.ui.SwipeToReveal
import aktual.core.ui.contrastingTextColor
import aktual.core.ui.isCompactWidth
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ListRulesItem(
  rule: Rule,
  isOpen: Boolean,
  onOpenChange: (Boolean) -> Unit,
  onAction: ListRulesActionHandler,
  modifier: Modifier = Modifier,
) {
  val deleteBackground = colors.errorText
  SwipeToReveal(
    actions =
      persistentListOf(
        SwipeAction(
          text = Strings.rulesItemDelete,
          icon = AktualIcons.Trash,
          background = deleteBackground,
          foreground = deleteBackground.contrastingTextColor(),
          onClick = {
            onOpenChange(false)
            onAction(Delete(rule.id))
          },
        ),
      ),
    isOpen = isOpen,
    onOpenChange = onOpenChange,
    modifier = modifier,
    shape = RowShape,
  ) {
    ListRulesItemRow(rule = rule, onClick = { onAction(Edit(rule.id)) })
  }
}

/** Keep this in sync with [ShimmerListRulesItem] */
@Composable
internal fun ListRulesItemRow(
  rule: Rule,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  query: String = "",
) {
  Box(modifier = modifier) {
    Row(
      modifier = Modifier.ruleRow(colors, onClick = onClick),
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = CenterVertically,
    ) {
      ItemContent(rule, query)
    }

    if (rule.stage.showBadge()) {
      RuleStageBadge(stage = rule.stage, modifier = Modifier.align(TopEnd))
    }
  }
}

// Only Pre/Post rules force an explicit ordering worth flagging; Default is the implicit norm.
@Stable
private fun RuleStage.showBadge() =
  when (this) {
    Default,
    Unknown -> false
    Pre,
    Post -> true
  }

@Composable
private fun RuleStageBadge(
  stage: RuleStage,
  modifier: Modifier = Modifier,
) {
  Chip(
    modifier = modifier.padding(Dimens.Large),
    text = stage.string(),
    colors = chipColors(colors),
    size = Small,
    textStyle = typography.labelSmall,
  )
}

@Stable
private fun chipColors(colors: Colors): ChipColors =
  ChipColors(
    background = colors.pillBackgroundSelected,
    content = colors.pillText,
  )

@Composable
private fun RowScope.ItemContent(rule: Rule, query: String) {
  val styles = rememberRuleSpanStyles()
  if (isCompactWidth()) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = CenterHorizontally,
    ) {
      ListRulesItemConditions(rule.conditions, styles, query)
      Text("↓", color = colors.tableText)
      ListRulesItemActions(rule.actions, styles, query)
    }
  } else {
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = CenterVertically,
    ) {
      ListRulesItemConditions(rule.conditions, styles, query, Modifier.weight(1f))
      Text("→", color = colors.tableText)
      ListRulesItemActions(rule.actions, styles, query, Modifier.weight(1f))
    }
  }
}

@Composable
private fun ListRulesItemConditions(
  conditions: ImmutableList<Condition>,
  styles: RuleSpanStyles,
  query: String,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
  ) {
    conditions.fastForEachIndexed { index, condition ->
      Box(
        modifier =
          Modifier.clip(CardShape)
            .background(colors.pillBackgroundLight, CardShape)
            .padding(horizontal = 5.dp, vertical = 3.dp),
      ) {
        val prefix = if (index != 0) "and " else ""
        Text(
          text = rememberConditionText(prefix, condition, styles, query),
          overflow = Ellipsis,
          maxLines = 1,
        )
      }
    }
  }
}

@Composable
private fun ListRulesItemActions(
  actions: ImmutableList<RuleAction>,
  styles: RuleSpanStyles,
  query: String,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
  ) {
    actions.fastForEach { action ->
      Box(
        modifier =
          Modifier.clip(CardShape)
            .background(colors.pillBackgroundLight, CardShape)
            .padding(horizontal = 5.dp, vertical = 3.dp),
      ) {
        Text(text = rememberActionText(action, styles, query), overflow = Ellipsis, maxLines = 1)
      }
    }
  }
}

/** Keep this in sync with [ListRulesItemRow] */
@Composable
internal fun ShimmerListRulesItem(modifier: Modifier = Modifier) {
  val shimmer = rememberShimmer(Window)

  Row(
    modifier = modifier.fillMaxWidth().ruleRow(colors, onClick = null).shimmer(shimmer),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f), horizontalAlignment = CenterHorizontally) {
      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.55f)
            .height(20.dp)
            .background(colors.pageText, CardShape),
      )

      Box(
        modifier =
          Modifier.padding(top = 4.dp)
            .fillMaxWidth(fraction = 0.35f)
            .height(20.dp)
            .background(colors.pageText, CardShape),
      )

      Box(
        modifier =
          Modifier.padding(top = 4.dp)
            .fillMaxWidth(fraction = 0.45f)
            .height(20.dp)
            .background(colors.pageText, CardShape),
      )
    }
  }
}

private fun Modifier.ruleRow(colors: Colors, onClick: (() -> Unit)?): Modifier =
  fillMaxWidth()
    .clip(RowShape)
    .background(colors.tableBackground, RowShape)
    .border(Hairline, colors.tableBorder, RowShape)
    .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
    .padding(horizontal = 15.dp, vertical = 12.dp)

@Preview
@Composable
private fun PreviewListRulesItem(
  @PreviewParameter(ListRulesItemProvider::class) params: ColorParams<ListRulesItemParams>,
) =
  PreviewWithColorParams(params) {
    ListRulesItem(
      rule = item,
      isOpen = isOpen,
      onOpenChange = {},
      onAction = {},
    )
  }

private data class ListRulesItemParams(
  val item: Rule,
  val isOpen: Boolean = false,
)

private class ListRulesItemProvider :
  ColorParamsProvider<ListRulesItemParams>(
    ListRulesItemParams(item = PreviewRule1),
    ListRulesItemParams(item = PreviewRule1.copy(stage = Pre)),
    ListRulesItemParams(item = PreviewRule2.copy(stage = Post)),
    ListRulesItemParams(item = PreviewRule1, isOpen = true),
  )

@Preview
@Composable
private fun PreviewShimmerListItem(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    ShimmerListRulesItem(modifier = Modifier.fillMaxWidth())
  }
