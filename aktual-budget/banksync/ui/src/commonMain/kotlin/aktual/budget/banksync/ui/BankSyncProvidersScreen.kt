package aktual.budget.banksync.ui

import aktual.budget.banksync.vm.providers.BankSyncProviderItem
import aktual.budget.banksync.vm.providers.BankSyncProvidersState
import aktual.budget.banksync.vm.providers.BankSyncProvidersViewModel
import aktual.core.icons.material.ArrowBack
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.BankSyncProviderSetupNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun BankSyncProvidersScreen(
  back: BackNavigator,
  setUp: BankSyncProviderSetupNavigator,
  modifier: Modifier = Modifier,
  viewModel: BankSyncProvidersViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbar = remember { SnackbarHostState() }

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        Reset -> snackbar.showProviderReset()
        is ResetFailed -> snackbar.showProviderResetFailed(event.error)
      }
    }
  }

  // check again on return, since one might have just been set up
  @Suppress("ComposeViewModelForwarding")
  LifecycleResumeEffect(viewModel) {
    viewModel.refresh()
    onPauseOrDispose {}
  }

  BankSyncProvidersScaffold(
    modifier = modifier,
    state = state,
    snackbarHostState = snackbar,
    onAction = { action ->
      when (action) {
        CloseProviders -> back()
        is SetUpProvider -> setUp(action.source)
        is ResetProvider -> viewModel.reset(action.source)
      }
    },
  )
}

@Composable
private fun BankSyncProvidersScaffold(
  state: BankSyncProvidersState,
  onAction: BankSyncProvidersActionHandler,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { scrollState.value.toFloat() }),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = { onAction(CloseProviders) }) },
        title = { Text(text = Strings.bankSyncProvidersTitle) },
      )
    },
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      when (state) {
        NoServer -> {
          FailureScreen(
            modifier = Modifier.padding(innerPadding),
            title = Strings.bankSyncProvidersTitle,
            reason = Strings.bankSyncProvidersNoServer,
            action =
              FailureAction(
                text = { Strings.navBack },
                icon = MaterialIcons.ArrowBack,
                onClick = { onAction(CloseProviders) },
              ),
          )
        }
        is Loaded -> {
          BankSyncProvidersContent(
            modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
            state = state,
            scrollState = scrollState,
            contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
            onAction = onAction,
          )
        }
      }
    }
  }
}

@Composable
private fun BankSyncProvidersContent(
  state: BankSyncProvidersState.Loaded,
  scrollState: ScrollState,
  contentPadding: PaddingValues,
  onAction: BankSyncProvidersActionHandler,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(contentPadding)
        .padding(BankSyncDS.settingsPadding),
    verticalArrangement = Arrangement.spacedBy(BankSyncDS.settingsFieldSpacing),
  ) {
    Text(
      text = Strings.bankSyncProvidersMessage,
      style = typography.bodyMedium,
      color = colors.pageTextSubdued,
    )

    for (item in state.providers) {
      ProviderItem(
        item = item,
        isResetting = state.resetting == item.source,
        canReset = state.resetting == null,
        onAction = onAction,
      )
    }

    BottomSpacing()
  }
}

@Composable
private fun ProviderItem(
  item: BankSyncProviderItem,
  isResetting: Boolean,
  canReset: Boolean,
  onAction: BankSyncProvidersActionHandler,
  modifier: Modifier = Modifier,
) {
  var showConfirm by remember { mutableStateOf(false) }
  val name = providerName(item.source)

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .background(colors.tableBackground, CardShape)
        .border(Hairline, colors.tableBorder, CardShape)
        .padding(BankSyncDS.itemCardPadding),
    horizontalArrangement = Arrangement.spacedBy(BankSyncDS.itemHorizontalSpacing),
    verticalAlignment = CenterVertically,
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(BankSyncDS.settingsLabelSpacing),
    ) {
      Text(text = name, style = typography.bodyLarge, color = colors.tableText)
      ProviderStatusChip(item.status)
    }

    when (item.status) {
      Configured -> {
        if (isResetting) {
          SyncingIndicator()
        } else {
          NormalTextButton(
            text = Strings.bankSyncProvidersReset,
            isEnabled = canReset,
            onClick = { showConfirm = true },
          )
        }
      }
      NotConfigured,
      Failed -> {
        NormalTextButton(
          text = Strings.bankSyncProvidersSetUp,
          onClick = { onAction(SetUpProvider(item.source)) },
        )
      }
      Checking,
      NoServer -> {
        // nothing to do until it's known whether it's set up
      }
    }
  }

  if (showConfirm) {
    ResetDialog(
      name = name,
      onDismiss = { showConfirm = false },
      onConfirm = {
        showConfirm = false
        onAction(ResetProvider(item.source))
      },
    )
  }
}

@Composable
private fun ResetDialog(name: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
  AktualAlertDialog(
    title = Strings.bankSyncProvidersResetTitle(name),
    onDismissRequest = onDismiss,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.bankSyncProvidersResetCancel) }
      TextButton(onClick = onConfirm) {
        Text(Strings.bankSyncProvidersResetConfirm, color = colors.errorText)
      }
    },
  ) {
    Text(Strings.bankSyncProvidersResetMessage)
  }
}

@PortraitPreview
@Composable
private fun PreviewBankSyncProvidersScaffold(
  @PreviewParameter(BankSyncProvidersStateProvider::class)
  params: ColoredParams<BankSyncProvidersState>
) = PreviewWithColoredParams(params) { BankSyncProvidersScaffold(state = this, onAction = {}) }

private val PreviewLoaded =
  BankSyncProvidersState.Loaded(
    providers =
      persistentListOf(
        BankSyncProviderItem(GoCardless, Configured),
        BankSyncProviderItem(EnableBanking, NotConfigured),
        BankSyncProviderItem(SimpleFin, Failed),
        BankSyncProviderItem(PluggyAi, Checking),
        BankSyncProviderItem(Akahu, NotConfigured),
      )
  )

private class BankSyncProvidersStateProvider :
  ColoredParameterProvider<BankSyncProvidersState>(
    PreviewLoaded,
    PreviewLoaded.copy(resetting = GoCardless),
    BankSyncProvidersState.NoServer,
  )
