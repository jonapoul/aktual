package aktual.budget.rules.ui.edit

import aktual.budget.model.Amount
import aktual.budget.model.Field
import aktual.budget.model.TransactionId
import aktual.budget.rules.components.label
import aktual.budget.rules.vm.edit.MatchChange
import aktual.budget.rules.vm.edit.MatchRow
import aktual.budget.rules.vm.edit.RuleMatchesState
import aktual.core.icons.AktualIcons
import aktual.core.icons.ViewShow
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardColumn
import aktual.core.ui.ColorParameters
import aktual.core.ui.Dimens
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.ShimmerBlock
import aktual.core.ui.checkbox
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.valentinilk.shimmer.ShimmerBounds.View
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.LocalDate

internal fun LazyListScope.matchesNode(
  state: RuleMatchesState,
  isEnabled: Boolean,
  onAction: OnAction<EditRuleAction>,
) {
  item(key = "matches") {
    TimelineRow(
      modifier = Modifier.animateItem(),
      hasRail = false,
      node = { IconNode(AktualIcons.ViewShow, colors.pillBackground, colors.pillText) },
    ) {
      NodeHeading(Strings.editRuleMatches) {
        Spacer(Modifier.weight(1f))
        if (state is Loaded && state.changing > 0) {
          AktualButton(
            text =
              if (state.selected.size == state.changing) {
                Strings.editRuleMatchesSelectNone
              } else {
                Strings.editRuleMatchesSelectAll
              },
            kind = Normal,
            isEnabled = isEnabled,
            onClick = { onAction(ToggleAllMatches) },
          )
          AktualButton(
            text = Strings.editRuleMatchesApply(state.selected.size),
            kind = Tonal,
            isEnabled = isEnabled && state.selected.isNotEmpty(),
            isLoading = state.isApplying,
            onClick = { onAction(ApplyActions) },
          )
        }
      }
      MatchesSummary(state)
    }
  }

  if (state is Loaded) {
    items(items = state.rows, key = { row -> "match-${row.id.value}" }) { row ->
      TimelineRow(modifier = Modifier.animateItem(), hasRail = false) {
        MatchCard(
          row = row,
          isSelected = row.id in state.selected,
          onToggle = { onAction(ToggleMatch(row.id)) }.takeIf { isEnabled && row.isChanged },
        )
      }
    }
  }
}

@Composable
private fun MatchesSummary(state: RuleMatchesState) =
  when (state) {
    Loading -> {
      val shimmer = rememberShimmer(View)
      ShimmerBlock(Modifier.fillMaxWidth(LOADING_WIDTH).height(20.dp).shimmer(shimmer))
    }
    Invalid -> SubduedText(Strings.editRuleMatchesInvalid)
    Failed -> SubduedText(Strings.editRuleMatchesFailed)
    is Loaded ->
      if (state.total == 0) {
        SubduedText(Strings.editRuleMatchesNone)
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Small)) {
          Text(
            text = Plurals.editRuleMatchesCount(state.total, state.total),
            style = typography.bodyLarge,
            color = colors.pageText,
          )
          SubduedText(
            modifier = Modifier.weight(1f),
            text = Plurals.editRuleMatchesChanging(state.changing, state.changing),
          )
        }
      }
  }

@Composable
private fun SubduedText(text: String, modifier: Modifier = Modifier) =
  Text(
    modifier = modifier,
    text = text,
    style = typography.bodyMedium,
    color = colors.pageTextSubdued,
  )

@Composable
private fun MatchCard(
  row: MatchRow,
  isSelected: Boolean,
  onToggle: (() -> Unit)?,
  modifier: Modifier = Modifier,
) =
  CardColumn(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(Dimens.Small),
    onClick = onToggle,
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(Dimens.Large),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (row.isChanged) {
        Checkbox(
          modifier = Modifier.size(24.dp),
          checked = isSelected,
          onCheckedChange = null,
          enabled = onToggle != null,
          colors = colors.checkbox(),
        )
      }
      Text(
        modifier = Modifier.weight(1f),
        text = row.payee.orEmpty(),
        style = typography.bodyLarge,
        color = colors.pageText,
        maxLines = 1,
        overflow = Ellipsis,
      )
      Text(
        text = row.amount.formattedString(),
        style = typography.bodyLarge,
        color = colors.pageText,
      )
    }
    Text(text = row.date.formatted(), style = typography.labelSmall, color = colors.pageTextSubdued)

    if (row.changes.isEmpty()) {
      SubduedText(Strings.editRuleMatchesNoChanges)
    } else {
      row.changes.fastForEach { change ->
        Text(
          text = change.string(),
          style = typography.bodyMedium,
          color = if (change == Deleted) colors.errorText else colors.noticeText,
          maxLines = 2,
          overflow = Ellipsis,
        )
      }
    }
  }

@Composable
private fun MatchChange.string(): String {
  val none = Strings.editRuleMatchesEmptyValue
  return when (this) {
    is Named -> Strings.editRuleMatchesChange(field.label(), from ?: none, to ?: none)
    is Moved -> Strings.editRuleMatchesChange(Field.Date.label(), from.formatted(), to.formatted())
    is Amounted ->
      Strings.editRuleMatchesChange(
        Field.Amount.label(),
        from.formattedString(),
        to.formattedString(),
      )
    is Cleared ->
      if (isCleared) Strings.editRuleMatchesCleared else Strings.editRuleMatchesUncleared
    is SplitInto -> Plurals.editRuleMatchesSplit(parts, parts)
    Deleted -> Strings.editRuleMatchesDeleted
  }
}

private const val LOADING_WIDTH = 0.5f

@Preview
@Composable
private fun PreviewMatchesNode(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    LazyColumn {
      matchesNode(
        state =
          RuleMatchesState.Loaded(
            total = 14,
            changing = 9,
            selected = persistentSetOf(TransactionId("a")),
            rows =
              persistentListOf(
                MatchRow(
                  id = TransactionId("a"),
                  date = LocalDate(2026, 3, 4),
                  amount = Amount(-2199),
                  payee = "Amazon",
                  changes =
                    persistentListOf(
                      MatchChange.Named(Category, null, "Shopping"),
                      MatchChange.Named(Notes, null, "online order"),
                    ),
                ),
                MatchRow(
                  id = TransactionId("b"),
                  date = LocalDate(2026, 3, 1),
                  amount = Amount(-849),
                  payee = "Amazon",
                  changes = persistentListOf(),
                ),
              ),
          ),
        isEnabled = true,
        onAction = {},
      )
    }
  }

@Preview
@Composable
private fun PreviewEmptyMatchesNode(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    LazyColumn {
      matchesNode(state = RuleMatchesState.Loaded(0, 0, persistentListOf()), isEnabled = true) {}
    }
  }
