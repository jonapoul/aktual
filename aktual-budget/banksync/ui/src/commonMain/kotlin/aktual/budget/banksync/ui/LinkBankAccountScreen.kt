package aktual.budget.banksync.ui

import aktual.budget.banksync.vm.link.BankLogin
import aktual.budget.banksync.vm.link.BankLoginStatus
import aktual.budget.banksync.vm.link.ExternalAccountItem
import aktual.budget.banksync.vm.link.ExternalAccounts
import aktual.budget.banksync.vm.link.LinkBankAccountState
import aktual.budget.banksync.vm.link.LinkBankAccountViewModel
import aktual.budget.banksync.vm.link.LinkTarget
import aktual.budget.banksync.vm.link.LoginAccountType
import aktual.budget.banksync.vm.link.LoginBankItem
import aktual.budget.banksync.vm.link.LoginBanks
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Amount
import aktual.core.icons.material.AccountBalance
import aktual.core.icons.material.ArrowBack
import aktual.core.icons.material.Key
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.BankSyncProvidersNavigator
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
import aktual.core.ui.formattedText
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun LinkBankAccountScreen(
  id: AccountId?,
  back: BackNavigator,
  providers: BankSyncProvidersNavigator,
  modifier: Modifier = Modifier,
  viewModel: LinkBankAccountViewModel = linkBankAccountViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbar = remember { SnackbarHostState() }
  val uriHandler = LocalUriHandler.current

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        Linked -> back()
        is LinkFailed -> snackbar.showLinkFailed(event.cause)
        is OpenBrowser -> uriHandler.openUri(event.url)
      }
    }
  }

  // check again on return from setting one up
  @Suppress("ComposeViewModelForwarding")
  LifecycleResumeEffect(viewModel) {
    viewModel.refreshProviders()
    onPauseOrDispose {}
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
        is SetOffBudget -> viewModel.setOffBudget(action.offBudget)
        is SelectCountry -> viewModel.selectCountry(action.country)
        is SelectAccountType -> viewModel.selectAccountType(action.type)
        is LogIn -> viewModel.logIn(action.bankId)
        ReopenLogin -> viewModel.reopenLogin()
        CancelLogin -> viewModel.cancelLogin()
        OpenProviders -> providers()
      }
    },
  )
}

@Composable
private fun linkBankAccountViewModel(id: AccountId?) =
  assistedMetroViewModel<LinkBankAccountViewModel, LinkBankAccountViewModel.Factory>(
    key = id?.value ?: NEW_ACCOUNT_KEY,
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
  var bankQuery by rememberSaveable { mutableStateOf("") }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = { onAction(CloseLink) }) },
        title = {
          val isNew = state is Choosing && state.target is New
          Text(text = if (isNew) Strings.bankSyncLinkNewTitle else Strings.bankSyncLinkTitle)
        },
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
        is NoProviders -> {
          FailureScreen(
            modifier = Modifier.padding(innerPadding),
            title = Strings.bankSyncLinkNoProvidersTitle,
            reason = Strings.bankSyncLinkNoProvidersMessage,
            icon = MaterialIcons.AccountBalance,
            action =
              if (state.hasServer) {
                FailureAction(
                  text = { Strings.bankSyncProvidersOpen },
                  icon = MaterialIcons.Key,
                  onClick = { onAction(OpenProviders) },
                )
              } else {
                backAction(onAction)
              },
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
            choosing(state, bankQuery, { bankQuery = it }, onAction)
          }
        }
      }
    }
  }
}

private const val NEW_ACCOUNT_KEY = "new"

// Whether the new account goes off budget
private val BudgetOptions = persistentListOf(false, true)

private fun backAction(onAction: LinkBankAccountActionHandler) =
  FailureAction(
    text = { Strings.navBack },
    icon = MaterialIcons.ArrowBack,
    onClick = { onAction(CloseLink) },
  )

private fun LazyListScope.choosing(
  state: LinkBankAccountState.Choosing,
  bankQuery: String,
  onBankQuery: (String) -> Unit,
  onAction: LinkBankAccountActionHandler,
) {
  item(key = "header") {
    Column(
      modifier = Modifier.fillMaxWidth().padding(BankSyncDS.headerPadding),
      verticalArrangement = Arrangement.spacedBy(BankSyncDS.settingsItemSpacing),
    ) {
      when (val target = state.target) {
        is Existing -> {
          Text(
            text = Strings.bankSyncLinkChoose(target.name ?: Strings.bankSyncUnnamedAccount),
            style = typography.bodyMedium,
            color = colors.pageText,
          )
        }
        is New -> {
          Text(
            text = Strings.bankSyncLinkNewChoose,
            style = typography.bodyMedium,
            color = colors.pageText,
          )
          AktualSlidingToggleButton(
            modifier = Modifier.fillMaxWidth(),
            selected = target.offBudget,
            options = BudgetOptions,
            onSelect = { onAction(SetOffBudget(it)) },
            string = {
              if (it) Strings.bankSyncLinkNewOffBudget else Strings.bankSyncLinkNewOnBudget
            },
            isEnabled = !state.isLinking,
          )
        }
      }

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
        ListingFailure(
          title = Strings.bankSyncLinkListFailed,
          cause = accounts.cause,
          onAction = onAction,
        )
      }
    }
    is NeedsLogin -> {
      bankLogin(state.selected, accounts.login, bankQuery, onBankQuery, onAction)
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
internal fun ListingFailure(
  title: String,
  cause: String?,
  onAction: LinkBankAccountActionHandler,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxWidth(),
    contentAlignment = Center,
  ) {
    FailureCard(
      title = title,
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
        text = balance.formattedText(),
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
  @PreviewParameter(LinkBankAccountStateProvider::class)
  params: ColoredParams<LinkBankAccountState>,
) = PreviewWithColoredParams(params) { LinkBankAccountScaffold(state = this, onAction = {}) }

private val PreviewAccounts =
  persistentListOf(
    ExternalAccountItem("ACT-1", "Checking", "Bankity Bank", Amount(1234.56)),
    ExternalAccountItem("ACT-2", "Credit Card", "Bankity Bank", Amount(-56.78)),
    ExternalAccountItem("ACT-3", "Savings", "Other Bank", Amount(500), linkedTo = "Savings"),
  )

private val PreviewBanks =
  LoginBanks.Loaded(
    persistentListOf(
      LoginBankItem("MONZO_MONZGB2L", "Monzo"),
      LoginBankItem("REVOLUT_REVOGB21", "Revolut", isBeta = true),
      LoginBankItem("STARLING_SRLGGB3L", "Starling"),
    ),
  )

private fun previewLogin(
  banks: LoginBanks = PreviewBanks,
  status: BankLoginStatus = Idle,
  accountType: LoginAccountType? = null,
) =
  previewChoosing(
    accounts =
      ExternalAccounts.NeedsLogin(
        BankLogin(persistentListOf("GB", "IE"), country = "GB", banks, status, accountType),
      ),
    selected = if (accountType == null) GoCardless else EnableBanking,
  )

private fun previewChoosing(
  accounts: ExternalAccounts,
  isLinking: Boolean = false,
  selected: AccountSyncSource = SimpleFin,
  target: LinkTarget = LinkTarget.Existing("Cash"),
) =
  LinkBankAccountState.Choosing(
    target = target,
    providers = persistentListOf(GoCardless, EnableBanking, SimpleFin),
    selected = selected,
    accounts = accounts,
    isLinking = isLinking,
  )

private class LinkBankAccountStateProvider :
  ColoredParameterProvider<LinkBankAccountState>(
    previewChoosing(ExternalAccounts.Loaded(PreviewAccounts)),
    previewChoosing(ExternalAccounts.Loaded(PreviewAccounts), isLinking = true),
    previewChoosing(ExternalAccounts.Loaded(PreviewAccounts), target = LinkTarget.New()),
    previewChoosing(Loading),
    previewChoosing(ExternalAccounts.Failure("Invalid access token")),
    previewLogin(),
    previewLogin(accountType = Business),
    previewLogin(banks = Loading),
    previewLogin(banks = LoginBanks.Failure("Invalid secret")),
    previewLogin(status = BankLoginStatus.Waiting("Monzo", link = "https://example.com")),
    previewLogin(status = BankLoginStatus.Failed(cause = null, isTimeout = true)),
    LinkBankAccountState.NoProviders(hasServer = true),
    LinkBankAccountState.NoProviders(hasServer = false),
    Loading,
    LinkBankAccountState.Failure(cause = null),
  )
