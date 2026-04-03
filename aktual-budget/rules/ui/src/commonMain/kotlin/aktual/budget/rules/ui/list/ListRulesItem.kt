package aktual.budget.rules.ui.list

import aktual.budget.model.Condition
import aktual.budget.model.RuleAction
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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

/** Keep this in sync with [ShimmerRuleListItem] */
@Composable
internal fun ListRulesItem(
  rule: RuleListItem,
  onAction: (ListRulesAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier = modifier.ruleRow(theme) { onAction(Open(rule.id)) },
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Checkbox(
      modifier = Modifier.minimumInteractiveComponentSize(),
      checked = false,
      onCheckedChange = null,
      colors = theme.checkbox(),
    )

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      val styles = rememberRuleSpanStyles()
      ListRulesItemConditions(rule.conditions, styles)
      Text("↓", color = theme.tableText)
      ListRulesItemActions(rule.actions, styles)
    }

    var showMenu by remember { mutableStateOf(false) }

    BareIconButton(
      imageVector = MaterialIcons.MoreVert,
      contentDescription = Strings.rulesItemMenu,
      onClick = { showMenu = true },
    )

    ListRulesItemMenu(
      rule = rule,
      expanded = showMenu,
      onDismiss = { showMenu = false },
      onAction = onAction,
    )
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
    horizontalAlignment = Alignment.Start,
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
    horizontalAlignment = Alignment.Start,
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

@Composable
private fun ListRulesItemMenu(
  rule: RuleListItem,
  expanded: Boolean,
  onDismiss: () -> Unit,
  onAction: (ListRulesAction) -> Unit,
) {
  ThemedDropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
    val editText = Strings.rulesItemEdit
    ThemedDropdownMenuItem(
      text = { Text(editText) },
      leadingIcon = { Icon(MaterialIcons.Edit, editText) },
      onClick = {
        onDismiss()
        onAction(Edit(rule))
      },
    )
    val deleteText = Strings.rulesItemDelete
    ThemedDropdownMenuItem(
      text = { Text(deleteText) },
      leadingIcon = { Icon(MaterialIcons.DeleteForever, deleteText) },
      onClick = {
        onDismiss()
        onAction(Delete(rule.id))
      },
    )
  }
}

/** Keep this in sync with [ListRulesItem] */
@Composable
internal fun ShimmerRuleListItem(modifier: Modifier = Modifier, theme: Theme = LocalTheme.current) {
  val shimmer = rememberShimmer(ShimmerBounds.Custom)

  Row(
    modifier =
      modifier.fillMaxWidth().ruleRow(theme, onClick = null).shimmer(shimmer).onGloballyPositioned {
        shimmer.updateBounds(it.unclippedBoundsInWindow())
      },
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // checkbox
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

private fun Modifier.ruleRow(theme: Theme, onClick: (() -> Unit)?): Modifier =
  fillMaxWidth()
    .clip(RowShape)
    .background(theme.tableBackground, RowShape)
    .border(Dp.Hairline, theme.tableBorder, RowShape)
    .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
    .padding(horizontal = 15.dp, vertical = 12.dp)

@Preview
@Composable
private fun PreviewRuleListItem(
  @PreviewParameter(RuleListItemProvider::class) params: ThemedParams<RuleListItem>
) = PreviewWithThemedParams(params) { ListRulesItem(this, onAction = {}) }

private class RuleListItemProvider :
  ThemedParameterProvider<RuleListItem>(PreviewRuleListItem1, PreviewRuleListItem2)

@Preview
@Composable
private fun PreviewShimmerListItem(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) { ShimmerRuleListItem(modifier = Modifier.fillMaxWidth()) }
