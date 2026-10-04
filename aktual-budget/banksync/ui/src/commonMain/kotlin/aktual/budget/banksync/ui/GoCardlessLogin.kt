package aktual.budget.banksync.ui

import aktual.budget.banksync.vm.link.GoCardlessBankItem
import aktual.budget.banksync.vm.link.GoCardlessLogin
import aktual.budget.banksync.vm.link.GoCardlessLoginStatus
import aktual.core.l10n.Strings
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.RowShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import java.util.Locale

/**
 * GoCardless lists accounts only after the user logs in to their bank, so this picks the bank, then
 * waits on the login. Banks are filtered by [query].
 */
internal fun LazyListScope.goCardlessLogin(
  login: GoCardlessLogin,
  query: String,
  onQuery: (String) -> Unit,
  onAction: LinkBankAccountActionHandler,
) {
  val status = login.status
  item(key = "gocardless-header") {
    GoCardlessHeader(
      login = login,
      query = query,
      onQuery = onQuery,
      isEnabled = status !is Waiting,
      onAction = onAction,
    )
  }

  when (status) {
    is Waiting -> {
      item(key = "gocardless-waiting") { LoginWaiting(status, onAction) }
      return
    }
    is Failed -> {
      item(key = "gocardless-failed") { LoginFailed(status) }
    }
    Idle -> {
      // Just the banks
    }
  }

  when (val banks = login.banks) {
    Loading -> {
      items(count = 5, key = { "bank-shimmer-$it" }) { ShimmerBankSyncAccountItem() }
    }
    is Failure -> {
      item(key = "gocardless-banks-failure") {
        ListingFailure(
          title = Strings.bankSyncLinkGocardlessBanksFailed,
          cause = banks.cause,
          onAction = onAction,
        )
      }
    }
    is Loaded -> {
      val filtered = banks.items.filter { it.name.contains(query.trim(), ignoreCase = true) }
      if (filtered.isEmpty()) {
        item(key = "gocardless-no-banks") {
          Text(
            modifier = Modifier.padding(BankSyncDS.headerPadding),
            text = Strings.bankSyncLinkGocardlessNoBanks,
            style = typography.bodyMedium,
            color = colors.pageTextSubdued,
          )
        }
      }
      items(filtered, key = { "bank-${it.id}" }) { bank ->
        BankRow(bank = bank, onClick = { onAction(LogIn(bank.id)) })
      }
    }
  }
}

@Composable
private fun GoCardlessHeader(
  login: GoCardlessLogin,
  query: String,
  onQuery: (String) -> Unit,
  isEnabled: Boolean,
  onAction: LinkBankAccountActionHandler,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().padding(BankSyncDS.headerPadding),
    verticalArrangement = Arrangement.spacedBy(BankSyncDS.settingsFieldSpacing),
  ) {
    Text(
      text = Strings.bankSyncLinkGocardlessIntro,
      style = typography.bodySmall,
      color = colors.pageTextSubdued,
    )

    Column(verticalArrangement = Arrangement.spacedBy(BankSyncDS.settingsLabelSpacing)) {
      Text(
        text = Strings.bankSyncLinkGocardlessCountry,
        style = typography.titleSmall,
        color = colors.pageTextLight,
      )
      AktualExposedDropDownMenu(
        modifier = Modifier.fillMaxWidth(),
        value = login.country,
        onValueChange = { onAction(SelectCountry(it)) },
        options = login.countries,
        string = { countryName(it) },
        isEnabled = isEnabled,
      )
    }

    BankSearchField(query = query, onQuery = onQuery, isEnabled = isEnabled)
  }
}

@Composable
private fun BankSearchField(
  query: String,
  onQuery: (String) -> Unit,
  isEnabled: Boolean,
  modifier: Modifier = Modifier,
) {
  val state = rememberTextFieldState(initialText = query)

  LaunchedEffect(state) { snapshotFlow { state.text.toString() }.collect(onQuery) }

  AktualTextField(
    modifier = modifier.fillMaxWidth(),
    state = state,
    singleLine = true,
    isEnabled = isEnabled,
    placeholderText = Strings.bankSyncLinkGocardlessSearch,
  )
}

@Composable
private fun LoginWaiting(
  status: GoCardlessLoginStatus.Waiting,
  onAction: LinkBankAccountActionHandler,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().padding(BankSyncDS.headerPadding),
    verticalArrangement = Arrangement.spacedBy(BankSyncDS.settingsFieldSpacing),
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(BankSyncDS.itemHorizontalSpacing),
      verticalAlignment = CenterVertically,
    ) {
      CircularProgressIndicator(
        modifier = Modifier.size(BankSyncDS.progressSize),
        color = colors.pageTextSubdued,
        strokeWidth = BankSyncDS.progressStroke,
      )
      Text(
        modifier = Modifier.weight(1f),
        text = Strings.bankSyncLinkGocardlessWaiting(status.bank),
        style = typography.bodyMedium,
        color = colors.pageText,
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(BankSyncDS.itemHorizontalSpacing)) {
      PrimaryTextButton(
        text = Strings.bankSyncLinkGocardlessReopen,
        onClick = { onAction(ReopenLogin) },
        isEnabled = status.link != null,
      )
      NormalTextButton(
        text = Strings.bankSyncLinkGocardlessCancel,
        onClick = { onAction(CancelLogin) },
      )
    }
  }
}

@Composable
private fun LoginFailed(status: GoCardlessLoginStatus.Failed, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxWidth().padding(BankSyncDS.headerPadding),
    verticalArrangement = Arrangement.spacedBy(BankSyncDS.itemContentSpacing),
  ) {
    Text(
      text = Strings.bankSyncLinkGocardlessFailed,
      style = typography.titleSmall,
      color = colors.errorText,
    )
    Text(
      text =
        if (status.isTimeout) {
          Strings.bankSyncLinkGocardlessTimeout
        } else {
          status.cause ?: Strings.bankSyncFailureMessage
        },
      style = typography.bodySmall,
      color = colors.pageTextSubdued,
    )
  }
}

@Composable
private fun BankRow(bank: GoCardlessBankItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Text(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.tableBackground, RowShape)
        .border(Hairline, colors.tableBorder, RowShape)
        .clickable(onClick = onClick)
        .padding(BankSyncDS.itemCardPadding),
    text = bank.name,
    style = typography.bodyMedium,
    color = colors.tableText,
    maxLines = 1,
    overflow = Ellipsis,
  )
}

// Named in the user's language, falling back to the ISO code
private fun countryName(code: String): String =
  Locale.Builder().setRegion(code).build().displayCountry.ifEmpty { code }
