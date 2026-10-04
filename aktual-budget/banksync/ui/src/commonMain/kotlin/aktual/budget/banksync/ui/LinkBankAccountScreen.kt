package aktual.budget.banksync.ui

import aktual.budget.banksync.vm.link.ExternalAccountItem
import aktual.budget.banksync.vm.link.ExternalAccounts
import aktual.budget.banksync.vm.link.LinkBankAccountState
import aktual.budget.banksync.vm.link.LinkBankAccountViewModel
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.core.icons.material.AccountBalance
import aktual.core.icons.material.ArrowBack
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureCard
import aktual.core.ui.FailureScreen
import aktual.core.ui.LoadingScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.RowShape
import aktual.core.ui.formattedString
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun LinkBankAccountScreen(
  id: AccountId,
  back: BackNavigator,
  modifier: Modifier = Modifier,
  viewModel: LinkBankAccountViewModel = linkBankAccountViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbar = remember { SnackbarHostState() }

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        Linked -> back()
        is LinkFailed -> snackbar.showLinkFailed(event.cause)
      }
    }
  }

  LinkBankAccountScaffold(
    modifier = modifier,
    state = state,
    snackbarHostState = snackbar,
    onAction = { action ->
      when (action) {
        CloseLink -> back()
        RetryListing -> viewModel.reload()
        is SelectProvider -> viewModel.select(action.source)
        is LinkTo -> viewModel.link(action.accountId)
      }
    },
  )
}

@Composable
private fun linkBankAccountViewModel(id: AccountId) =
  assistedMetroViewModel<LinkBankAccountViewModel, LinkBankAccountViewModel.Factory>(
    key = id.value
  ) {
    create(id)
  }

@Composable
private fun LinkBankAccountScaffold(
  state: LinkBankAccountState,
  onAction: LinkBankAccountActionHandler,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = { onAction(CloseLink) }) },
        title = { Text(text = Strings.bankSyncLinkTitle) },
        actions = {
          if (state is Choosing && state.isLinking) {
            SyncingIndicator()
          }
        },
      )
    },
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      when (state) {
        Loading -> {
          LoadingScreen(modifier = Modifier.padding(innerPadding))
        }
        is Failure -> {
          FailureScreen(
            modifier = Modifier.padding(innerPadding),
            title = Strings.bankSyncLinkFailureTitle,
            reason = state.cause ?: Strings.bankSyncSettingsFailureMissing,
            action = backAction(onAction),
          )
        }
        NoProviders -> {
          FailureScreen(
            modifier = Modifier.padding(innerPadding),
            title = Strings.bankSyncLinkNoProvidersTitle,
            reason = Strings.bankSyncLinkNoProvidersMessage,
            icon = MaterialIcons.AccountBalance,
            action = backAction(onAction),
          )
        }
        is Choosing -> {
          LazyColumn(
            modifier =
              Modifier.hazedTopBarContent(hazeState, innerPadding)
                .scrollbar(listState)
                .padding(BankSyncDS.listPadding),
            state = listState,
            contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
            verticalArrangement = Arrangement.spacedBy(BankSyncDS.listItemSpacing),
          ) {
            choosing(state, onAction)
          }
        }
      }
    }
  }
}

private fun backAction(onAction: LinkBankAccountActionHandler) =
  FailureAction(
    text = { Strings.navBack },
    icon = MaterialIcons.ArrowBack,
    onClick = { onAction(CloseLink) },
  )

private fun LazyListScope.choosing(
  state: LinkBankAccountState.Choosing,
  onAction: LinkBankAccountActionHandler,
) {
  item(key = "header") {
    Column(
      modifier = Modifier.fillMaxWidth().padding(BankSyncDS.headerPadding),
      verticalArrangement = Arrangement.spacedBy(BankSyncDS.settingsItemSpacing),
    ) {
      Text(
        text = Strings.bankSyncLinkChoose(state.accountName ?: Strings.bankSyncUnnamedAccount),
        style = typography.bodyMedium,
        color = colors.pageText,
      )

      if (state.providers.size > 1) {
        AktualSlidingToggleButton(
          modifier = Modifier.fillMaxWidth(),
          selected = state.selected,
          options = state.providers,
          onSelect = { source -> onAction(SelectProvider(source)) },
          string = { source -> providerName(source) },
          isEnabled = !state.isLinking,
        )
      } else {
        Text(
          text = providerName(state.selected),
          style = typography.titleSmall,
          color = colors.pageTextLight,
        )
      }
    }
  }

  when (val accounts = state.accounts) {
    Loading -> {
      items(count = 5, key = { "shimmer-$it" }) { ShimmerBankSyncAccountItem() }
    }
    is Failure -> {
      item(key = "failure") {
        ListingFailure(cause = accounts.cause, onAction = onAction)
      }
    }
    is Loaded -> {
      if (accounts.items.isEmpty()) {
        item(key = "empty") {
          Text(
            modifier = Modifier.padding(BankSyncDS.headerPadding),
            text = Strings.bankSyncLinkEmpty,
            style = typography.bodyMedium,
            color = colors.pageTextSubdued,
          )
        }
      }
      items(accounts.items, key = { it.accountId }) { item ->
        ExternalAccountRow(
          item = item,
          enabled = !state.isLinking && item.linkedTo == null,
          onClick = { onAction(LinkTo(item.accountId)) },
        )
      }
    }
  }

  item(key = "bottom") { BottomSpacing() }
}

@Composable
private fun ListingFailure(
  cause: String?,
  onAction: LinkBankAccountActionHandler,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxWidth(),
    contentAlignment = Center,
  ) {
    FailureCard(
      title = Strings.bankSyncLinkListFailed,
      reason = cause ?: Strings.bankSyncFailureMessage,
      action =
        FailureAction(
          text = { Strings.syncRetry },
          icon = MaterialIcons.Refresh,
          onClick = { onAction(RetryListing) },
        ),
    )
  }
}

@Composable
private fun ExternalAccountRow(
  item: ExternalAccountItem,
  enabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.tableBackground, RowShape)
        .border(Hairline, colors.tableBorder, RowShape)
        .clickable(enabled = enabled, onClick = onClick)
        .padding(BankSyncDS.itemCardPadding),
    horizontalArrangement = Arrangement.spacedBy(BankSyncDS.itemHorizontalSpacing),
    verticalAlignment = CenterVertically,
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(BankSyncDS.itemContentSpacing),
    ) {
      Text(
        text = item.name,
        style = typography.bodyMedium,
        fontWeight = SemiBold,
        color = if (item.linkedTo == null) colors.tableText else colors.pageTextSubdued,
        maxLines = 1,
        overflow = Ellipsis,
      )

      item.institution?.let { institution ->
        Text(
          text = institution,
          style = typography.bodySmall,
          color = colors.pageTextSubdued,
          maxLines = 1,
          overflow = Ellipsis,
        )
      }

      item.linkedTo?.let { linkedTo ->
        Text(
          text = Strings.bankSyncLinkLinkedTo(linkedTo),
          style = typography.bodySmall,
          color = colors.pageTextSubdued,
          maxLines = 1,
          overflow = Ellipsis,
        )
      }
    }

    item.balance?.let { balance ->
      Text(
        text = balance.formattedString(),
        style = typography.bodyMedium,
        color = if (balance.isPositive()) colors.tableText else colors.errorText,
        maxLines = 1,
      )
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewLinkBankAccountScaffold(
  @PreviewParameter(LinkBankAccountStateProvider::class) params: ColoredParams<LinkBankAccountState>
) = PreviewWithColoredParams(params) { LinkBankAccountScaffold(state = this, onAction = {}) }

private val PreviewAccounts =
  persistentListOf(
    ExternalAccountItem("ACT-1", "Checking", "Bankity Bank", Amount(1234.56)),
    ExternalAccountItem("ACT-2", "Credit Card", "Bankity Bank", Amount(-56.78)),
    ExternalAccountItem("ACT-3", "Savings", "Other Bank", Amount(500), linkedTo = "Savings"),
  )

private fun previewChoosing(accounts: ExternalAccounts, isLinking: Boolean = false) =
  LinkBankAccountState.Choosing(
    accountName = "Cash",
    providers = persistentListOf(SimpleFin, Akahu),
    selected = SimpleFin,
    accounts = accounts,
    isLinking = isLinking,
  )

private class LinkBankAccountStateProvider :
  ColoredParameterProvider<LinkBankAccountState>(
    previewChoosing(ExternalAccounts.Loaded(PreviewAccounts)),
    previewChoosing(ExternalAccounts.Loaded(PreviewAccounts), isLinking = true),
    previewChoosing(Loading),
    previewChoosing(ExternalAccounts.Failure("Invalid access token")),
    NoProviders,
    Loading,
    LinkBankAccountState.Failure(cause = null),
  )
