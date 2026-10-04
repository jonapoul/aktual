package aktual.budget.home.ui

import aktual.budget.home.domain.AccountBalance
import aktual.budget.home.domain.AccountSection
import aktual.budget.home.domain.AccountSyncState
import aktual.budget.home.domain.AccountSyncState.Failed
import aktual.budget.home.domain.AccountSyncState.Ok
import aktual.budget.home.domain.AccountsSummary
import aktual.budget.home.vm.AccountsCardState
import aktual.budget.home.vm.AccountsCardState.Loaded
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.AnimatedLoading
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.RounderCardShape
import aktual.core.ui.formattedString
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp.Companion.Hairline
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.persistentListOf

private val CardPadding = 16.dp
private val RowMinHeight = 48.dp
private val SyncDotSize = 8.dp

@Composable
internal fun AccountsCard(
  state: AccountsCardState,
  onClickAccount: (AccountId) -> Unit,
  onClickSetUp: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RounderCardShape)
        .background(colors.tableBackground, RounderCardShape)
        .border(Hairline, colors.tableBorder, RounderCardShape)
        .padding(vertical = CardPadding)
  ) {
    HeaderRow(
      title = Strings.homeAccountsTitle,
      amount = (state as? Loaded)?.summary?.netWorth,
      style = typography.titleSmall,
    )

    when (state) {
      Loading -> AccountsLoading()
      Empty -> AccountsEmpty(onClickSetUp)
      is Loaded -> AccountsContent(state.summary, onClickAccount)
    }
  }
}

@Composable
@Suppress("UnusedReceiverParameter")
private fun ColumnScope.AccountsContent(
  summary: AccountsSummary,
  onClickAccount: (AccountId) -> Unit,
) {
  AccountsSection(Strings.homeAccountsOnBudget, summary.onBudget, onClickAccount)
  AccountsSection(Strings.homeAccountsOffBudget, summary.offBudget, onClickAccount)
}

@Composable
private fun AccountsSection(
  title: String,
  section: AccountSection,
  onClickAccount: (AccountId) -> Unit,
) {
  if (section.accounts.isEmpty()) return

  HeaderRow(
    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    title = title.uppercase(),
    amount = section.total,
    style = typography.labelMedium,
  )

  section.accounts.fastForEach { account ->
    AccountRow(account = account, onClick = { onClickAccount(account.id) })
  }
}

@Composable
private fun HeaderRow(
  title: String,
  amount: Amount?,
  style: TextStyle,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = CardPadding),
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
      Text(
        text = amount.formattedString(),
        style = style.tabularFigures(),
        fontWeight = SemiBold,
        color = colors.pageTextSubdued,
        maxLines = 1,
      )
    }
  }
}

@Composable
private fun AccountRow(
  account: AccountBalance,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = RowMinHeight)
        .clickable(onClick = onClick)
        .padding(horizontal = CardPadding),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = account.name,
      style = typography.bodyLarge,
      color = colors.pageText,
      maxLines = 1,
      overflow = Ellipsis,
    )

    if (account.syncState is Failed) {
      val description = Strings.homeAccountsSyncFailed
      Box(
        modifier =
          Modifier.size(SyncDotSize).background(colors.errorText, CircleShape).semantics {
            contentDescription = description
          }
      )
    }

    Text(
      text = account.balance.formattedString(),
      style = typography.bodyLarge.tabularFigures(),
      fontWeight = SemiBold,
      color = if (account.balance < Zero) colors.numberNegative else colors.pageText,
      maxLines = 1,
    )
  }
}

@Composable
private fun AccountsLoading(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxWidth().padding(CardPadding), contentAlignment = Center) {
    AnimatedLoading(modifier = Modifier.size(32.dp))
  }
}

@Composable
private fun AccountsEmpty(onClickSetUp: () -> Unit, modifier: Modifier = Modifier) {
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

    PrimaryTextButton(text = Strings.homeAccountsEmptyAction, onClick = onClickSetUp)
  }
}

private fun TextStyle.tabularFigures() = copy(fontFeatureSettings = "tnum")

@Preview
@Composable
private fun PreviewAccountsCard(
  @PreviewParameter(AccountsCardStateProvider::class) params: ColoredParams<AccountsCardState>
) =
  PreviewWithColoredParams(params) {
    AccountsCard(
      modifier = Modifier.padding(16.dp),
      state = this,
      onClickAccount = {},
      onClickSetUp = {},
    )
  }

private class AccountsCardStateProvider :
  ColoredParameterProvider<AccountsCardState>(
    Loaded(PREVIEW_ACCOUNTS),
    Loaded(PREVIEW_ACCOUNTS.copy(offBudget = section())),
    Empty,
    Loading,
  )

internal val PREVIEW_ACCOUNTS =
  AccountsSummary(
    onBudget =
      section(
        account("Current Account", 2_431.18),
        account("Joint Account", 1_204.50),
        account("Easy Saver", 8_500.00, Ok(lastSync = null)),
        account("Credit Card", -642.37, Failed(ReauthRequired)),
        account("Cash", 60.00),
      ),
    offBudget =
      section(
        account("Stocks ISA", 14_220.91),
        account("Pension", 38_114.02),
        account("Car Loan", -6_300.00),
      ),
    closed = persistentListOf(),
  )

private fun section(vararg accounts: AccountBalance) =
  AccountSection(
    accounts = persistentListOf(*accounts),
    total = accounts.fold(Amount.Zero) { sum, account -> sum + account.balance },
  )

private fun account(name: String, balance: Double, syncState: AccountSyncState = NotLinked) =
  AccountBalance(AccountId(name), name, Amount(balance), syncState)
