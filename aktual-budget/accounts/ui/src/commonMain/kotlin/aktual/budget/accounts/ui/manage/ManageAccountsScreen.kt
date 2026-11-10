package aktual.budget.accounts.ui.manage

import aktual.budget.accounts.vm.manage.AccountSection
import aktual.budget.accounts.vm.manage.Failure
import aktual.budget.accounts.vm.manage.Loading
import aktual.budget.accounts.vm.manage.ManageAccountsState
import aktual.budget.accounts.vm.manage.ManageAccountsViewModel
import aktual.budget.accounts.vm.manage.Success
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.Refresh
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.CreateAccountNavigator
import aktual.core.nav.TransactionsNavigator
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.LocalNavDrawerOpener
import aktual.core.ui.LocalSnackbars
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.OnAction
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
internal fun ManageAccountsScreen(
  back: BackNavigator,
  toCreateAccount: CreateAccountNavigator,
  toTransactions: TransactionsNavigator,
  isRoot: Boolean,
  modifier: Modifier = Modifier,
  viewModel: ManageAccountsViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbars = LocalSnackbars.current
  var overlay by remember { mutableStateOf<ManageOverlay?>(null) }

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        is AccountMoved -> snackbars.show { showAccountMoved(event, viewModel::undo) }
        is GroupMoved -> snackbars.show { showGroupMoved(event, viewModel::undo) }
        is Reopened -> snackbars.show { showReopened(event) }
        is GroupNameTaken -> snackbars.show { showGroupNameTaken(event) }
        is ActionFailed -> snackbars.show { showActionFailed() }
      }
    }
  }

  val onAction: OnAction<ManageAccountsAction> = { action ->
    when (action) {
      NavigateBack -> back()
      Retry -> viewModel.retry()
      ToggleClosed -> viewModel.toggleClosed()
      NewAccount -> toCreateAccount()
      is Show -> overlay = action.overlay
      is Reopen -> viewModel.reopen(action.id)
      is CreateGroup -> viewModel.createGroup(action.name)
      is RenameGroup -> viewModel.renameGroup(action.id, action.name)
      is ViewTransactions -> toTransactions(action.id)
      is DeleteGroup -> viewModel.deleteGroup(action.id)
    }
  }

  ManageAccountsScaffold(modifier = modifier, state = state, isRoot = isRoot, onAction = onAction)

  overlay?.let { current ->
    ManageOverlays(
      overlay = current,
      state = state as? Success,
      // A sheet closing after opening another leaves the new one showing
      onDismiss = { closed -> overlay = overlay.takeIf { it != closed } },
      onAction = onAction,
      onClose = { event -> snackbars.show { showClosed(event, viewModel::undo) } },
      onDelete = { event -> snackbars.show { showDeleted(event) } },
    )
  }
}

@Composable
private fun ManageAccountsScaffold(
  state: ManageAccountsState,
  isRoot: Boolean,
  onAction: OnAction<ManageAccountsAction>,
  modifier: Modifier = Modifier,
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = {
          if (isRoot && LocalNavDrawerOpener.current != null) {
            NavDrawerIconButton()
          } else {
            NavBackIconButton(onClick = { onAction(NavigateBack) })
          }
        },
        title = { Text(text = Strings.accountsManageTitle) },
        actions = { AddMenu(onAction) },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      Box(modifier = Modifier.hazedTopBarContent(hazeState, innerPadding)) {
        val padding = hazedTopBarContentPadding(hazeState, innerPadding)
        when (state) {
          Loading ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Center) {
              CircularProgressIndicator(color = colors.pageTextSubdued)
            }

          is Failure ->
            FailureScreen(
              modifier = Modifier.padding(padding),
              title = Strings.accountsManageFailed,
              reason = state.message,
              action =
                FailureAction(
                  text = { Strings.syncRetry },
                  icon = AktualIcons.Refresh,
                  onClick = { onAction(Retry) },
                ),
            )

          is Success ->
            AccountsList(
              state = state,
              listState = listState,
              contentPadding = padding,
              onAction = onAction,
            )
        }
      }
    }
  }
}

@Composable
private fun AddMenu(onAction: OnAction<ManageAccountsAction>, modifier: Modifier = Modifier) {
  var expanded by remember { mutableStateOf(false) }
  Box(modifier = modifier) {
    BareIconButton(
      imageVector = AktualIcons.Add,
      contentDescription = Strings.accountsManageAdd,
      onClick = { expanded = true },
    )
    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      AktualDropdownMenuItem(
        text = Strings.accountsManageNewAccount,
        onClick = {
          expanded = false
          onAction(NewAccount)
        },
      )
      AktualDropdownMenuItem(
        text = Strings.accountsManageNewGroup,
        onClick = {
          expanded = false
          onAction(Show(CreateGroupDialog))
        },
      )
    }
  }
}

@Composable
private fun AccountsList(
  state: Success,
  listState: LazyListState,
  contentPadding: PaddingValues,
  onAction: OnAction<ManageAccountsAction>,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
  ) {
    section(key = "on", title = { Strings.accountsManageOnBudget }, state.onBudget, onAction)
    section(key = "off", title = { Strings.accountsManageOffBudget }, state.offBudget, onAction)

    if (state.closed.isNotEmpty()) {
      item(key = "closed") {
        ClosedHeader(
          modifier = Modifier.animateItem().padding(top = ManageAccountsDS.sectionSpacing),
          count = state.closed.size,
          isExpanded = state.closedExpanded,
          onToggle = { onAction(ToggleClosed) },
        )
      }
      if (state.closedExpanded) {
        items(state.closed, key = { "closed-${it.id.value}" }) { account ->
          ClosedAccountRow(
            modifier = Modifier.animateItem(),
            account = account,
            onReopen = { onAction(Reopen(account.id)) },
          )
        }
      }
    }

    item(key = "bottom") {
      Spacer(Modifier.height(Dimens.ScreenPadding))
      BottomSpacing()
    }
  }
}

private fun LazyListScope.section(
  key: String,
  title: @Composable () -> String,
  section: AccountSection,
  onAction: OnAction<ManageAccountsAction>,
) {
  item(key = "$key-header") {
    SectionHeader(modifier = Modifier.animateItem(), title = title(), total = section.total)
  }
  section.groups.fastForEach { group ->
    val groupKey = "$key-group-${group.id?.value}"
    item(key = groupKey) {
      GroupHeader(modifier = Modifier.animateItem(), group = group, onAction = onAction)
    }
    if (group.accounts.isEmpty()) {
      item(key = "$groupKey-empty") { EmptyGroupRow(modifier = Modifier.animateItem()) }
    }
    items(group.accounts, key = { "$key-account-${it.id.value}" }) { account ->
      AccountRow(
        modifier = Modifier.animateItem(),
        account = account,
        onClick = { onAction(Show(AccountMenu(account))) },
      )
    }
  }
}

internal object ManageAccountsDS {
  val sectionSpacing = 16.dp
  val rowHeight = 60.dp
  val headerHeight = 44.dp
  val rowPadding = 12.dp
}

@PortraitPreview
@Composable
private fun PreviewManageAccounts(
  @PreviewParameter(ManageAccountsStateProvider::class) params: ColorParams<ManageAccountsState>,
) =
  PreviewWithColorParams(params) {
    ManageAccountsScaffold(state = this, isRoot = false, onAction = {})
  }

private class ManageAccountsStateProvider :
  ColorParamsProvider<ManageAccountsState>(
    ManageAccountsPreview.success,
    ManageAccountsPreview.success.copy(closedExpanded = false),
    Loading,
    Failure("Database connection lost"),
  )
