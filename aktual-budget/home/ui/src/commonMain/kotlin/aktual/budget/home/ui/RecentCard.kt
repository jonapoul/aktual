package aktual.budget.home.ui

import aktual.budget.home.vm.RecentCardState
import aktual.budget.home.vm.RecentCardState.Loaded
import aktual.budget.home.vm.RecentTransaction
import aktual.budget.home.vm.TransferDirection
import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PreviewWithColorParams
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

private val RowMinHeight = 56.dp

// Shows nothing when there are no transactions
@Composable
internal fun RecentCard(
  state: RecentCardState,
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
) {
  if (state == Empty) return

  HomeCard(modifier = modifier) {
    when (state) {
      Empty -> Unit
      Loading -> ShimmerRecent()
      Failed -> CardError(message = Strings.homeRecentFailed, onAction = onAction)
      is Loaded -> RecentContent(state, onAction)
    }
  }
}

@Composable
private fun RecentContent(
  state: Loaded,
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    RecentHeader(onSeeAll = { onAction(OpenTransactions) })

    state.transactions.fastForEach { transaction ->
      RecentRow(
        transaction = transaction,
        onClick = { onAction(OpenTransaction(transaction.id)) },
      )
    }
  }
}

// Keep this in sync with RecentHeader and RecentRow
@Composable
private fun ShimmerRecent(modifier: Modifier = Modifier) {
  val bar = Modifier.background(colors.tableText, CardShape)

  Column(modifier = modifier.fillMaxWidth().shimmer(rememberShimmer(Window))) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = CardPadding),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Box(modifier = bar.width(160.dp).height(24.dp))
      Box(modifier = bar.width(48.dp).height(24.dp))
    }

    repeat(SHIMMER_ROWS) {
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .padding(horizontal = CardPadding, vertical = 6.dp),
        horizontalArrangement = spacedBy(12.dp),
        verticalAlignment = CenterVertically,
      ) {
        Box(modifier = bar.width(DateBadgeWidth).height(40.dp))

        Column(modifier = Modifier.weight(1f), verticalArrangement = spacedBy(6.dp)) {
          Box(modifier = bar.width(120.dp).height(16.dp))
          Box(modifier = bar.width(160.dp).height(12.dp))
        }

        Box(modifier = bar.width(64.dp).height(16.dp))
      }
    }
  }
}

@Composable
private fun RecentHeader(onSeeAll: () -> Unit, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = CardPadding),
    horizontalArrangement = spacedBy(12.dp),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f).semantics { heading() },
      text = Strings.homeRecentTitle,
      style = typography.titleSmall,
      fontWeight = SemiBold,
      color = colors.pageTextSubdued,
      maxLines = 1,
      overflow = Ellipsis,
    )

    CardLink(text = Strings.homeRecentSeeAll, onClick = onSeeAll)
  }
}

@Composable
private fun RecentRow(
  transaction: RecentTransaction,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = RowMinHeight)
        .hoverClickable(onClick = onClick)
        .padding(horizontal = CardPadding, vertical = 6.dp),
    horizontalArrangement = spacedBy(12.dp),
    verticalAlignment = CenterVertically,
  ) {
    DateBadge(transaction.date)

    Column(modifier = Modifier.weight(1f), verticalArrangement = spacedBy(2.dp)) {
      Text(
        text = transaction.payeeLabel(),
        style = typography.bodyLarge,
        color = colors.pageText,
        maxLines = 1,
        overflow = Ellipsis,
      )

      Text(
        text = transaction.detail(),
        style = typography.bodySmall,
        color = if (transaction.needsCategory) colors.warningText else colors.pageTextSubdued,
        maxLines = 1,
        overflow = Ellipsis,
      )
    }

    AmountText(
      amount = transaction.amount,
      style = typography.bodyLarge,
      color = if (transaction.amount < Zero) colors.numberNegative else colors.pageText,
    )
  }
}

@Composable
private fun RecentTransaction.payeeLabel(): String =
  when (transfer) {
    To -> Strings.transactionsTransferTo(payee.orEmpty())
    From -> Strings.transactionsTransferFrom(payee.orEmpty())
    null -> payee ?: if (isSplit) Strings.transactionsSplitNoPayee else ""
  }

@Composable
private fun RecentTransaction.detail(): String {
  val category =
    when {
      isSplit -> Strings.transactionsSplit
      needsCategory -> Strings.transactionsNeedsCategory
      else -> category
    }
  return when {
    account == null -> category.orEmpty()
    category == null -> account.orEmpty()
    else -> Strings.homeRecentRowDetail(account.orEmpty(), category)
  }
}

private const val SHIMMER_ROWS = 3

@Preview
@Composable
private fun PreviewRecentCard(
  @PreviewParameter(RecentCardStateProvider::class) params: ColorParams<RecentCardState>,
) =
  PreviewWithColorParams(params) {
    RecentCard(modifier = Modifier.padding(16.dp), state = this, onAction = {})
  }

private class RecentCardStateProvider :
  ColorParamsProvider<RecentCardState>(PREVIEW_RECENT, Loading, Failed)

internal val PREVIEW_RECENT =
  Loaded(
    persistentListOf(
      recent("Corner Shop", "Current Account", "Groceries", day = 14, amount = -12.40),
      recent("Joint Account", "Current Account", null, day = 13, amount = -500.0, transfer = To),
      recent("Coffee Co.", "Credit Card", null, day = 12, amount = -3.20, needsCategory = true),
      recent("Supermarket", "Joint Account", null, day = 11, amount = -64.15, isSplit = true),
      recent("Employer Ltd", "Current Account", "Salary", day = 10, amount = 2_650.00),
    ),
  )

@Suppress("LongParameterList")
private fun recent(
  payee: String,
  account: String,
  category: String?,
  day: Int,
  amount: Double,
  transfer: TransferDirection? = null,
  needsCategory: Boolean = false,
  isSplit: Boolean = false,
) =
  RecentTransaction(
    id = TransactionId("$payee-$day"),
    date = LocalDate(2026, 4, day),
    payee = payee,
    transfer = transfer,
    account = account,
    category = category,
    amount = Amount(amount),
    needsCategory = needsCategory,
    isSplit = isSplit,
  )
