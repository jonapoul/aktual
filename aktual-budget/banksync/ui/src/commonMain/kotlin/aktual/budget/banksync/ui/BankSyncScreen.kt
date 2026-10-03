package aktual.budget.banksync.ui

import aktual.budget.banksync.vm.BankSyncEvent
import aktual.budget.banksync.vm.BankSyncState
import aktual.budget.banksync.vm.BankSyncViewModel
import aktual.budget.banksync.vm.Empty
import aktual.budget.banksync.vm.Failure
import aktual.budget.banksync.vm.Loading
import aktual.budget.banksync.vm.Success
import aktual.core.icons.material.AccountBalance
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.Sync
import aktual.core.l10n.Strings
import aktual.core.nav.BankSyncSettingsNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.HazedPullToRefreshBox
import aktual.core.ui.LocalBottomSpacing
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.NoticeBanner
import aktual.core.ui.PageBackground
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.bottomNavBarPadding
import aktual.core.ui.hazedTopBar
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
internal fun BankSyncScreen(
  settings: BankSyncSettingsNavigator,
  modifier: Modifier = Modifier,
  viewModel: BankSyncViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbar = remember { SnackbarHostState() }

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        is BankSyncEvent.Finished -> snackbar.showBankSyncSummary(event.summary)
      }
    }
  }

  // refresh on return, so the relative "last bank sync" times don't go stale
  @Suppress("ComposeViewModelForwarding")
  LifecycleResumeEffect(viewModel) {
    viewModel.reload(showLoading = false)
    onPauseOrDispose {}
  }

  BankSyncScaffold(
    modifier = modifier,
    state = state,
    snackbarHostState = snackbar,
    onAction = { action ->
      when (action) {
        Reload -> viewModel.reload()
        SyncAll -> viewModel.syncAll()
        is SyncAccount -> viewModel.sync(action.id)
        is OpenSettings -> settings(action.id)
      }
    },
  )
}

@Composable
private fun BankSyncScaffold(
  state: BankSyncState,
  onAction: BankSyncActionHandler,
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
        navigationIcon = { NavDrawerIconButton() },
        title = { Text(text = Strings.bankSyncTitle) },
        actions = {
          if (state is Success && state.canSync && state.providers.isNotEmpty()) {
            SyncAllButton(isSyncing = state.isSyncing, onClick = { onAction(SyncAll) })
          }
        },
      )
    },
    snackbarHost = {
      SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(bottom = LocalBottomSpacing.current + bottomNavBarPadding()),
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()

      HazedPullToRefreshBox(
        modifier = Modifier.padding(BankSyncDS.listPadding),
        contentAlignment = Center,
        onRefresh = { onAction(Reload) },
        isRefreshing = state is Loading,
        hazeState = hazeState,
        innerPadding = innerPadding,
      ) { padding ->
        BankSyncContent(
          state = state,
          contentPadding = padding,
          onAction = onAction,
          listState = listState,
        )
      }
    }
  }
}

@Composable
private fun BankSyncContent(
  state: BankSyncState,
  contentPadding: PaddingValues,
  onAction: BankSyncActionHandler,
  listState: LazyListState,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = CenterHorizontally,
  ) {
    when (state) {
      Loading -> {
        Column(
          modifier = Modifier.padding(contentPadding),
          verticalArrangement = Arrangement.spacedBy(BankSyncDS.listItemSpacing),
        ) {
          repeat(times = 10) { ShimmerBankSyncAccountItem() }
          BottomSpacing()
        }
      }
      Empty -> {
        FailureScreen(
          title = Strings.bankSyncEmpty,
          reason = Strings.bankSyncNotice,
          icon = MaterialIcons.AccountBalance,
          background = colors.tableBackground,
          action = null,
        )
      }
      is Failure -> {
        FailureScreen(
          title = Strings.bankSyncFailureTitle,
          reason = state.cause ?: Strings.bankSyncFailureMessage,
          background = colors.tableBackground,
          action =
            FailureAction(
              text = { Strings.syncRetry },
              icon = MaterialIcons.Refresh,
              onClick = { onAction(Reload) },
            ),
        )
      }
      is Success -> {
        ContentSuccess(
          state = state,
          listState = listState,
          contentPadding = contentPadding,
          onAction = onAction,
        )
      }
    }
  }
}

@Composable
private fun ContentSuccess(
  state: Success,
  listState: LazyListState,
  contentPadding: PaddingValues,
  onAction: BankSyncActionHandler,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(BankSyncDS.listItemSpacing),
  ) {
    item(key = "notice") {
      NoticeBanner(text = Strings.bankSyncNotice, icon = MaterialIcons.AccountBalance)
    }

    for ((source, status, accounts) in state.providers) {
      item(key = "provider-${source.value}") {
        ProviderHeader(source = source, status = status)
      }
      items(accounts, key = { it.id.value }) { account ->
        BankSyncAccountItem(
          account = account,
          isLinked = true,
          onClick = { onAction(OpenSettings(account.id)) },
          sync =
            if (state.canSync) {
              AccountSync(
                enabled = !state.isSyncing,
                onClick = { onAction(SyncAccount(account.id)) },
              )
            } else {
              null
            },
        )
      }
    }

    if (state.unlinked.isNotEmpty()) {
      item(key = "unlinked") { UnlinkedHeader() }
      items(state.unlinked, key = { it.id.value }) { account ->
        BankSyncAccountItem(account = account, isLinked = false)
      }
    }

    item { BottomSpacing() }
  }
}

@Composable
private fun SyncAllButton(isSyncing: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
  if (isSyncing) {
    SyncingIndicator(modifier.padding(BankSyncDS.topBarProgressPadding))
  } else {
    BareIconButton(
      modifier = modifier,
      imageVector = MaterialIcons.Sync,
      contentDescription = Strings.bankSyncSyncAll,
      onClick = onClick,
    )
  }
}

@Preview
@Composable
private fun PreviewBankSyncScaffold(
  @PreviewParameter(BankSyncStateProvider::class) params: ColoredParams<BankSyncState>
) = PreviewWithColoredParams(params) { BankSyncScaffold(state = this, onAction = {}) }

private class BankSyncStateProvider :
  ColoredParameterProvider<BankSyncState>(
    BankSyncPreview.success,
    BankSyncPreview.syncing,
    BankSyncPreview.unlinkedOnly,
    Empty,
    Loading,
    Failure("Some problem happened"),
  )
