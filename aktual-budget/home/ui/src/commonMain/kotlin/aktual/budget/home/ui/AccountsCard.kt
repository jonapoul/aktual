package aktual.budget.home.ui

import aktual.budget.home.vm.AccountBalance
import aktual.budget.home.vm.AccountSection
import aktual.budget.home.vm.AccountsCardState
import aktual.budget.home.vm.AccountsCardState.Loaded
import aktual.budget.home.vm.AccountsSummary
import aktual.budget.home.vm.mostRecentlyActive
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncState
import aktual.budget.model.AccountSyncState.Ok
import aktual.budget.model.Amount
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AccountSyncDot
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.ButtonShape
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.NewTransactionsBadge
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PrimaryTextButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

private val RowMinHeight = 48.dp

// From the end of a row to its balance, past the new transaction button. Header totals and the
// shimmer use it too, so they line up with the balances. The button is 40dp, with a smaller filled
// square drawn inside it so the touch target stays full size
private val RowEndPadding = 4.dp
private val NewTransactionButtonGap = 6.dp
private val AmountEndInset = RowEndPadding + 40.dp + NewTransactionButtonGap

@Composable
internal fun AccountsCard(
  state: AccountsCardState,
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
  isBoxed: Boolean = true,
  showAll: Boolean = false,
) {
  HomeCard(modifier = modifier.animateContentSize(), isBoxed = isBoxed) {
    HeaderRow(
      title = Strings.homeAccountsTitle,
      amount = (state as? Loaded)?.summary?.netWorth,
      style = typography.titleSmall,
    )

    when (state) {
      Loading -> ShimmerAccounts()
      Empty -> AccountsEmpty(onAction)
      Failed ->
        CardError(
          modifier = Modifier.padding(top = 4.dp),
          message = Strings.homeAccountsFailed,
          onAction = onAction,
        )
      is Loaded -> AccountsContent(state, showAll, onAction)
    }
  }
}

@Composable
private fun ColumnScope.AccountsContent(
  state: Loaded,
  showAll: Boolean,
  onAction: HomeActionHandler,
) {
  var isExpanded by rememberSaveable { mutableStateOf(false) }
  val recent = if (showAll) null else state.recent
  val visibleIds =
    remember(state, recent, isExpanded) {
      val summary = if (isExpanded || recent == null) state.summary else recent
      (summary.onBudget.accounts + summary.offBudget.accounts).map { it.id }.toImmutableSet()
    }

  AccountsSection(Strings.homeAccountsOnBudget, state.summary.onBudget, visibleIds, onAction)
  AccountsSection(Strings.homeAccountsOffBudget, state.summary.offBudget, visibleIds, onAction)

  if (recent != null) {
    val allCount = with(state.summary) { onBudget.accounts.size + offBudget.accounts.size }
    ExpandRow(
      text =
        if (isExpanded) Strings.homeAccountsShowLess else Strings.homeAccountsShowAll(allCount),
      onClick = { isExpanded = !isExpanded },
    )
  }
}

@Composable
private fun ExpandRow(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = RowMinHeight)
        .hoverClickable(role = Button, onClick = onClick)
        .padding(horizontal = CardPadding),
    verticalAlignment = CenterVertically,
  ) {
    Text(text = text, style = typography.bodyMedium, color = colors.pageTextSubdued)
  }
}

@Composable
private fun ColumnScope.AccountsSection(
  title: String,
  section: AccountSection,
  visibleIds: ImmutableSet<AccountId>,
  onAction: HomeActionHandler,
) {
  AnimatedVisibility(
    visible = section.accounts.fastAny { it.id in visibleIds },
    enter = RowEnter,
    exit = RowExit,
  ) {
    HeaderRow(
      modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
      title = title.uppercase(),
      amount = section.total,
      style = typography.labelMedium,
    )
  }

  section.accounts.fastForEach { account ->
    key(account.id) {
      AnimatedVisibility(visible = account.id in visibleIds, enter = RowEnter, exit = RowExit) {
        AccountRow(
          account = account,
          onClick = { onAction(OpenAccount(account.id)) },
          onNewTransaction = { onAction(NewTransaction(account.id)) },
        )
      }
    }
  }
}

private val RowEnter = expandVertically() + fadeIn()
private val RowExit = shrinkVertically() + fadeOut()

@Composable
private fun HeaderRow(
  title: String,
  amount: Amount?,
  style: TextStyle,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(start = CardPadding, end = AmountEndInset),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f).semantics { heading() },
      text = title,
      style = style,
      fontWeight = SemiBold,
      color = colors.pageTextSubdued,
      maxLines = 1,
      overflow = Ellipsis,
    )

    if (amount != null) {
      AmountText(amount = amount, style = style, color = colors.pageTextSubdued)
    }
  }
}

@Composable
private fun AccountRow(
  account: AccountBalance,
  onClick: () -> Unit,
  onNewTransaction: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = RowMinHeight)
        .hoverClickable(onClick = onClick)
        .padding(start = CardPadding, end = RowEndPadding),
    horizontalArrangement = Arrangement.spacedBy(NewTransactionButtonGap),
    verticalAlignment = CenterVertically,
  ) {
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = CenterVertically,
    ) {
      Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = CenterVertically,
      ) {
        Text(
          modifier = Modifier.weight(1f, fill = false),
          text = account.name,
          style = typography.bodyLarge,
          color = colors.pageText,
          maxLines = 1,
          overflow = Ellipsis,
        )

        NewTransactionsBadge(account.newTransactions)
      }

      AccountSyncDot(account.syncState)

      AmountText(
        amount = account.balance,
        style = typography.bodyLarge,
        color = if (account.balance < Zero) colors.numberNegative else colors.pageText,
      )
    }

    val newTransaction = Strings.homeAccountsNewTransaction(account.name)
    BareIconButton(
      imageVector = AktualIcons.Add,
      contentDescription = newTransaction,
      onClick = onNewTransaction,
    ) {
      Box(
        modifier = Modifier.size(28.dp).background(colors.buttonPrimaryBackground, ButtonShape),
        contentAlignment = Center,
      ) {
        Icon(
          modifier = Modifier.size(18.dp),
          imageVector = AktualIcons.Add,
          contentDescription = newTransaction,
          tint = colors.buttonPrimaryText,
        )
      }
    }
  }
}

// Keep this in sync with AccountRow
@Composable
private fun ShimmerAccounts(modifier: Modifier = Modifier) {
  val bar = Modifier.background(colors.tableText, CardShape)

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(start = CardPadding, end = AmountEndInset)
        .padding(top = 4.dp)
        .shimmer(rememberShimmer(Window)),
  ) {
    repeat(SHIMMER_ROWS) {
      Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = RowMinHeight),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = CenterVertically,
      ) {
        Box(modifier = bar.width(140.dp).height(16.dp))
        Box(modifier = bar.width(72.dp).height(16.dp))
      }
    }
  }
}

private const val SHIMMER_ROWS = 3

@Composable
private fun AccountsEmpty(onAction: HomeActionHandler, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = CardPadding).padding(top = 12.dp),
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(
      text = Strings.homeAccountsEmpty,
      style = typography.bodyMedium,
      color = colors.pageTextSubdued,
    )

    PrimaryTextButton(text = Strings.homeAccountsEmptyAction, onClick = { onAction(AddAccount) })
  }
}

@Preview
@Composable
private fun PreviewAccountsCard(
  @PreviewParameter(AccountsCardStateProvider::class) params: ColorParams<AccountsCardState>,
) =
  PreviewWithColorParams(params) {
    AccountsCard(
      modifier = Modifier.padding(16.dp),
      state = this,
      onAction = {},
    )
  }

private class AccountsCardStateProvider :
  ColorParamsProvider<AccountsCardState>(
    Loaded(PREVIEW_ACCOUNTS),
    Loaded(PREVIEW_ACCOUNTS, recent = PREVIEW_ACCOUNTS.mostRecentlyActive(limit = 5)),
    Loaded(PREVIEW_ACCOUNTS.copy(offBudget = section())),
    Empty,
    Loading,
    Failed,
  )

internal val PREVIEW_ACCOUNTS =
  AccountsSummary(
    onBudget =
      section(
        account("Current Account", 2_431.18, daysAgo = 0, newTransactions = 3),
        account("Joint Account", 1_204.50, daysAgo = 1),
        account("Easy Saver", 8_500.00, Ok(lastSync = null), daysAgo = 12),
        account(
          "Credit Card",
          -642.37,
          AccountSyncState.Failed(ReauthRequired),
          daysAgo = 2,
          newTransactions = 1,
        ),
        account("Cash", 60.00, daysAgo = 40),
      ),
    offBudget =
      section(
        account("Stocks ISA", 14_220.91, daysAgo = 5),
        account("Pension", 38_114.02, daysAgo = 30),
        account("Car Loan", -6_300.00),
      ),
    closed = persistentListOf(),
  )

private fun section(vararg accounts: AccountBalance) =
  AccountSection(
    accounts = persistentListOf(*accounts),
    total = accounts.fold(Zero) { sum, account -> sum + account.balance },
  )

private fun account(
  name: String,
  balance: Double,
  syncState: AccountSyncState = NotLinked,
  daysAgo: Int? = null,
  newTransactions: Int = 0,
) =
  AccountBalance(
    id = AccountId(name),
    name = name,
    balance = Amount(balance),
    syncState = syncState,
    lastActivity = daysAgo?.let { LocalDate(year = 2026, month = 3, day = 31).minus(it, DAY) },
    newTransactions = newTransactions,
  )
