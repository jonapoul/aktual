package aktual.budget.home.ui

import aktual.budget.home.domain.OverspentCategory
import aktual.budget.home.vm.AttentionCardState
import aktual.budget.home.vm.AttentionCardState.Loaded
import aktual.budget.home.vm.AttentionItem
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.core.icons.AktualIcons
import aktual.core.icons.CloudWarning
import aktual.core.icons.material.CalendarToday
import aktual.core.icons.material.FormatListBulleted
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Warning
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareTextButton
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private val RowMinHeight = 48.dp
private val IconSize = 20.dp

// Only shown when there's something to act on
@Composable
internal fun AttentionCard(
  state: AttentionCardState,
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
) {
  if (state == Failed) {
    HomeCard(modifier = modifier) {
      CardError(message = Strings.homeAttentionFailed, onAction = onAction)
    }
    return
  }

  if (state !is Loaded) return

  var showOverspent by rememberSaveable { mutableStateOf(false) }

  HomeCard(modifier = modifier) {
    Text(
      modifier = Modifier.padding(horizontal = CardPadding).semantics { heading() },
      text = Strings.homeAttentionTitle,
      style = typography.titleSmall,
      fontWeight = SemiBold,
      color = colors.pageTextSubdued,
      maxLines = 1,
      overflow = Ellipsis,
    )

    state.items.fastForEach { item ->
      AttentionRow(item = item, onAction = onAction, onShowOverspent = { showOverspent = true })
    }
  }

  val overspent = state.items.firstNotNullOfOrNull { it as? Overspent }
  if (showOverspent && overspent != null) {
    OverspentSheet(categories = overspent.categories, onDismiss = { showOverspent = false })
  }
}

// These two can only be fixed by linking the account again
private fun AttentionItem.SyncFailed.fixAction(): HomeAction =
  when (status) {
    ReauthRequired,
    AccountMissing -> LinkBankAccount(account)
    else -> OpenBankSyncSettings(account)
  }

@Composable
private fun AttentionRow(
  item: AttentionItem,
  onAction: HomeActionHandler,
  onShowOverspent: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier.fillMaxWidth().heightIn(min = RowMinHeight).padding(horizontal = CardPadding),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = CenterVertically,
  ) {
    Icon(
      modifier = Modifier.size(IconSize),
      imageVector = item.icon(),
      contentDescription = item.iconDescription(),
      tint = item.tint(),
    )

    Text(
      modifier = Modifier.weight(1f),
      text = item.label(),
      style = typography.bodyLarge,
      color = colors.pageText,
      maxLines = 2,
      overflow = Ellipsis,
    )

    BareTextButton(
      text = item.actionLabel(),
      onClick = {
        when (item) {
          is SyncFailed -> onAction(item.fixAction())
          is SyncFailedMany -> onAction(OpenBankSync)
          is Uncategorised -> onAction(ReviewUncategorised)
          is Overspent -> onShowOverspent()
          is OverdueSchedules -> onAction(OpenSchedules)
        }
      },
    )
  }
}

private fun AttentionItem.icon(): ImageVector =
  when (this) {
    is SyncFailed,
    is SyncFailedMany -> AktualIcons.CloudWarning
    is Uncategorised -> MaterialIcons.FormatListBulleted
    is Overspent -> MaterialIcons.Warning
    is OverdueSchedules -> MaterialIcons.CalendarToday
  }

@Composable
private fun AttentionItem.iconDescription(): String =
  when (this) {
    is SyncFailed,
    is SyncFailedMany -> Strings.homeAttentionIconSync
    is Uncategorised -> Strings.homeAttentionIconUncategorised
    is Overspent -> Strings.homeAttentionIconOverspent
    is OverdueSchedules -> Strings.homeAttentionIconOverdue
  }

@Composable
@ReadOnlyComposable
private fun AttentionItem.tint(): Color =
  when (this) {
    is SyncFailed,
    is SyncFailedMany -> colors.numberNegative
    is Uncategorised,
    is Overspent,
    is OverdueSchedules -> colors.warningText
  }

@Composable
private fun AttentionItem.label(): String =
  when (this) {
    is SyncFailed -> label()
    is SyncFailedMany -> Plurals.homeAttentionSyncFailedMany(count, count)
    is Uncategorised -> Plurals.homeAttentionUncategorised(count, count)
    is Overspent -> Plurals.homeAttentionOverspent(categories.size, categories.size)
    is OverdueSchedules -> Plurals.homeAttentionOverdue(count, count)
  }

@Composable
private fun AttentionItem.SyncFailed.label(): String =
  when (status) {
    ReauthRequired -> Strings.homeAttentionSyncReauth(name)
    AttentionRequired -> Strings.homeAttentionSyncAttention(name)
    RateLimitExceeded -> Strings.homeAttentionSyncRateLimited(name)
    TimedOut -> Strings.homeAttentionSyncTimedOut(name)
    AccountMissing -> Strings.homeAttentionSyncAccountMissing(name)
    else -> Strings.homeAttentionSyncFailed(name)
  }

@Composable
private fun AttentionItem.actionLabel(): String =
  when (this) {
    is SyncFailed,
    is SyncFailedMany -> Strings.homeAttentionFix
    is Uncategorised,
    is Overspent,
    is OverdueSchedules -> Strings.homeAttentionReview
  }

@Composable
private fun OverspentSheet(
  categories: ImmutableList<OverspentCategory>,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualModalBottomSheet(
    modifier = modifier,
    onDismissRequest = onDismiss,
    sheetState = rememberBottomSheetState(initialValue = Hidden),
  ) {
    OverspentContent(categories)
  }
}

@Composable
private fun OverspentContent(
  categories: ImmutableList<OverspentCategory>,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp)
        .padding(bottom = 20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(
      modifier = Modifier.semantics { heading() },
      text = Strings.homeAttentionOverspentSheetTitle,
      style = typography.headlineSmall,
    )

    categories.fastForEach { category ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = CenterVertically,
      ) {
        Text(
          modifier = Modifier.weight(1f),
          text = category.name,
          style = typography.bodyLarge,
          color = colors.pageText,
          maxLines = 1,
          overflow = Ellipsis,
        )

        AmountText(
          amount = category.balance,
          style = typography.bodyLarge,
          color = colors.numberNegative,
        )
      }
    }
  }
}

@Preview
@Composable
private fun PreviewAttentionCard(
  @PreviewParameter(AttentionCardStateProvider::class) params: ColoredParams<AttentionCardState>,
) =
  PreviewWithColoredParams(params) {
    AttentionCard(
      modifier = Modifier.padding(16.dp),
      state = this,
      onAction = {},
    )
  }

@Preview
@Composable
private fun PreviewOverspentContent(
  @PreviewParameter(OverspentProvider::class) params: ColoredParams<AttentionItem.Overspent>,
) =
  PreviewWithColoredParams(params) {
    OverspentContent(categories)
  }

private val PREVIEW_OVERSPENT =
  AttentionItem.Overspent(
    persistentListOf(
      OverspentCategory(CategoryId("food"), "Food", Amount(-42.18)),
      OverspentCategory(CategoryId("fuel"), "Fuel", Amount(-12.00)),
    ),
  )

internal val PREVIEW_ATTENTION =
  Loaded(
    persistentListOf(
      AttentionItem.SyncFailed(AccountId("card"), "Credit Card", ReauthRequired),
      AttentionItem.Uncategorised(count = 7),
      PREVIEW_OVERSPENT,
      AttentionItem.OverdueSchedules(count = 1),
    ),
  )

private class AttentionCardStateProvider :
  ColoredParameterProvider<AttentionCardState>(
    PREVIEW_ATTENTION,
    Loaded(persistentListOf(AttentionItem.Uncategorised(count = 1))),
    Loaded(
      persistentListOf(
        AttentionItem.SyncFailedMany(count = 4),
        AttentionItem.OverdueSchedules(count = 3),
      ),
    ),
    Failed,
  )

private class OverspentProvider :
  ColoredParameterProvider<AttentionItem.Overspent>(PREVIEW_OVERSPENT)
