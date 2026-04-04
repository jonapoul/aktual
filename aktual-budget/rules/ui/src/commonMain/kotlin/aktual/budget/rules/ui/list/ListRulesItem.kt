package aktual.budget.rules.ui.list

import aktual.budget.model.Condition
import aktual.budget.model.RuleAction
import aktual.budget.rules.vm.CheckboxesState
import aktual.budget.rules.vm.CheckboxesState.Active
import aktual.budget.rules.vm.CheckboxesState.Inactive
import aktual.budget.rules.vm.RuleListItem
import aktual.core.icons.material.DeleteForever
import aktual.core.icons.material.Edit
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.MoreVert
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.BareIconButton
import aktual.core.ui.CardShape
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.PreviewWithThemedParams
import aktual.core.ui.RowShape
import aktual.core.ui.ThemeParameters
import aktual.core.ui.ThemedDropdownMenu
import aktual.core.ui.ThemedDropdownMenuItem
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.checkbox
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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.unclippedBoundsInWindow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentSetOf

/** Keep this in sync with [ShimmerRuleListItem] */
@Composable
internal fun ListRulesItem(
  rule: RuleListItem,
  checkboxes: CheckboxesState,
  onAction: (ListRulesAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val isChecked = checkboxes is Active && rule.id in checkboxes.ids

  val toggleCheck =
    if (checkboxes is Active) {
      { onAction(if (isChecked) Uncheck(rule.id) else Check(rule.id)) }
    } else {
      null
    }

  Row(
    modifier =
      modifier.ruleRow(theme, onOpen = { onAction(Open(rule.id)) }, onToggle = toggleCheck),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (checkboxes is Active) {
      Checkbox(
        modifier = Modifier.minimumInteractiveComponentSize(),
        checked = rule.id in checkboxes.ids,
        onCheckedChange = { toggleCheck?.invoke() },
        colors = theme.checkbox(),
      )
    }

    ItemContent(rule)

    var showMenu by remember { mutableStateOf(false) }

    Box {
      BareIconButton(
        imageVector = MaterialIcons.MoreVert,
        contentDescription = Strings.rulesItemMenu,
        onClick = { showMenu = true },
      )

      val onDismiss = { showMenu = false }

      ThemedDropdownMenu(expanded = showMenu, onDismissRequest = onDismiss) {
        ThemedDropdownMenuItem(
          text = Strings.rulesItemEdit,
          leadingIcon = MaterialIcons.Edit,
          onClick = {
            onDismiss()
            onAction(Edit(rule))
          },
        )
        ThemedDropdownMenuItem(
          text = Strings.rulesItemDelete,
          leadingIcon = MaterialIcons.DeleteForever,
          onClick = {
            onDismiss()
            onAction(Delete(rule.id))
          },
        )
      }
    }
  }
}

@Composable
private fun RowScope.ItemContent(rule: RuleListItem, theme: Theme = LocalTheme.current) {
  val styles = rememberRuleSpanStyles()
  if (isCompactWidth()) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      ListRulesItemConditions(rule.conditions, styles)
      Text("↓", color = theme.tableText)
      ListRulesItemActions(rule.actions, styles)
    }
  } else {
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      ListRulesItemConditions(rule.conditions, styles, modifier = Modifier.weight(1f))
      Text("→", color = theme.tableText)
      ListRulesItemActions(rule.actions, styles, modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun ListRulesItemConditions(
  conditions: ImmutableList<Condition>,
  styles: RuleSpanStyles,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
  ) {
    conditions.fastForEachIndexed { index, condition ->
      Box(
        modifier =
          Modifier.clip(CardShape)
            .background(theme.pillBackgroundLight, CardShape)
            .padding(horizontal = 5.dp, vertical = 3.dp)
      ) {
        val prefix = if (index != 0) "and " else ""
        Text(
          text = rememberConditionText(prefix, condition, styles),
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
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
  ) {
    actions.fastForEach { action ->
      Box(
        modifier =
          Modifier.clip(CardShape)
            .background(theme.pillBackgroundLight, CardShape)
            .padding(horizontal = 5.dp, vertical = 3.dp)
      ) {
        Text(text = rememberActionText(action, styles), overflow = Ellipsis, maxLines = 1)
      }
    }
  }
}

/** Keep this in sync with [ListRulesItem] */
@Composable
internal fun ShimmerRuleListItem(
  checkboxes: CheckboxesState,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val shimmer = rememberShimmer(ShimmerBounds.Custom)

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .ruleRow(theme, onOpen = null, onToggle = null)
        .shimmer(shimmer)
        .onGloballyPositioned { shimmer.updateBounds(it.unclippedBoundsInWindow()) },
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // checkbox
    if (checkboxes is Active) {
      Box(
        modifier = Modifier.size(LocalMinimumInteractiveComponentSize.current),
        contentAlignment = Alignment.Center,
      ) {
        Box(
          modifier =
            Modifier.size(LocalMinimumInteractiveComponentSize.current / 2)
              .background(theme.pageText, CardShape)
        )
      }
    }

    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.55f)
            .height(20.dp)
            .background(theme.pageText, CardShape)
      )

      Box(
        modifier =
          Modifier.padding(top = 4.dp)
            .fillMaxWidth(fraction = 0.35f)
            .height(20.dp)
            .background(theme.pageText, CardShape)
      )

      Box(
        modifier =
          Modifier.padding(top = 4.dp)
            .fillMaxWidth(fraction = 0.45f)
            .height(20.dp)
            .background(theme.pageText, CardShape)
      )
    }

    Box(
      modifier = Modifier.size(LocalMinimumInteractiveComponentSize.current),
      contentAlignment = Alignment.Center,
    ) {
      Box(
        modifier =
          Modifier.size(LocalMinimumInteractiveComponentSize.current / 2)
            .background(theme.pageText, CardShape)
      )
    }
  }
}

private fun Modifier.ruleRow(
  theme: Theme,
  onOpen: (() -> Unit)?,
  onToggle: (() -> Unit)?,
): Modifier {
  val onClick = onToggle ?: onOpen
  return fillMaxWidth()
    .clip(RowShape)
    .background(theme.tableBackground, RowShape)
    .border(Dp.Hairline, theme.tableBorder, RowShape)
    .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
    .padding(horizontal = 15.dp, vertical = 12.dp)
}

@Preview
@Composable
private fun PreviewRuleListItem(
  @PreviewParameter(RuleListItemProvider::class) params: ThemedParams<RuleListItemParams>
) =
  PreviewWithThemedParams(params) {
    ListRulesItem(rule = item, checkboxes = checkboxes, onAction = {})
  }

private data class RuleListItemParams(
  val item: RuleListItem,
  val checkboxes: CheckboxesState = Inactive,
)

private class RuleListItemProvider :
  ThemedParameterProvider<RuleListItemParams>(
    RuleListItemParams(item = PreviewRuleListItem1),
    RuleListItemParams(
      item = PreviewRuleListItem2,
      checkboxes = Active(persistentSetOf(PreviewRuleListItem2.id)),
    ),
  )

@Preview
@Composable
private fun PreviewShimmerListItem(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    ShimmerRuleListItem(checkboxes = Inactive, modifier = Modifier.fillMaxWidth())
  }
