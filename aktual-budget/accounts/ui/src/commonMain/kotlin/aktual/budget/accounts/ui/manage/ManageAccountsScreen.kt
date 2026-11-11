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
import aktual.core.ui.dragHandle
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
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
      is MoveAccount -> viewModel.moveAccount(action.id, action.group, action.before)
      is MoveGroup -> viewModel.moveGroup(action.id, action.before)
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
  val scope = rememberCoroutineScope()
  val latestOnAction by rememberUpdatedState(onAction)
  val drag =
    remember(listState, scope) {
      ManageDragState(
        listState = listState,
        scope = scope,
        moveAccount = { id, group, before -> latestOnAction(MoveAccount(id, group, before)) },
        moveGroup = { id, before -> latestOnAction(MoveGroup(id, before)) },
      )
    }
  val sections =
    remember(state.onBudget, state.offBudget) {
      ManageSections(state.onBudget.groups, state.offBudget.groups)
    }
  SideEffect { drag.sections = sections }

  val density = LocalDensity.current
  val edge = with(density) { ManageAccountsDS.autoScrollEdge.toPx() }
  val speed = with(density) { ManageAccountsDS.autoScrollSpeed.toPx() }
  val held = drag.held
  LaunchedEffect(held) { if (held != null) drag.autoScroll(held, edge, speed) }

  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
  ) {
    section(
      side = OnBudget,
      title = { Strings.accountsManageOnBudget },
      section = state.onBudget,
      lines = drag.lines(OnBudget, sections),
      drag = drag,
      onAction = onAction,
    )
    section(
      side = OffBudget,
      title = { Strings.accountsManageOffBudget },
      section = state.offBudget,
      lines = drag.lines(OffBudget, sections),
      drag = drag,
      onAction = onAction,
    )

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
  side: Side,
  title: @Composable () -> String,
  section: AccountSection,
  lines: List<ManageLine>,
  drag: ManageDragState,
  onAction: OnAction<ManageAccountsAction>,
) {
  item(key = "${side.key}-header") {
    SectionHeader(modifier = Modifier.animateItem(), title = title(), total = section.total)
  }
  lines.fastForEach { line ->
    item(key = line.key, contentType = line::class) {
      DraggableLine(line = line, drag = drag, onAction = onAction)
    }
  }
}

@Composable
private fun LazyItemScope.DraggableLine(
  line: ManageLine,
  drag: ManageDragState,
  onAction: OnAction<ManageAccountsAction>,
) {
  if (line is EmptyLine) {
    EmptyGroupRow(modifier = Modifier.animateItem())
    return
  }

  val haptics = LocalHapticFeedback.current
  val moveUp = Strings.accountsManageMoveUp
  val moveDown = Strings.accountsManageMoveDown
  val isDragged = drag.dragged == line.key
  val elevation by animateDpAsState(if (drag.held == line.key) 8.dp else 0.dp)
  val modifier =
    Modifier.zIndex(if (isDragged) 1f else 0f)
      .then(if (isDragged) Modifier else Modifier.animateItem())
      .graphicsLayer { translationY = drag.offsetOf(line.key) }
      .shadow(elevation)
      .semantics {
        customActions = buildList {
          if (drag.canMoveBy(line, -1)) {
            add(CustomAccessibilityAction(moveUp) { true.also { drag.moveBy(line, -1) } })
          }
          if (drag.canMoveBy(line, 1)) {
            add(CustomAccessibilityAction(moveDown) { true.also { drag.moveBy(line, 1) } })
          }
        }
      }
  val handle =
    Modifier.dragHandle(
      key = line.key,
      haptics = haptics,
      pickUp = { grabY -> drag.pickUp(line, grabY) },
      dragBy = { delta -> drag.dragBy(line.key, delta) },
      drop = { drag.drop(line.key) },
    )

  when (line) {
    is GroupLine ->
      GroupHeader(
        modifier = modifier,
        group = line.group,
        handleModifier = handle,
        onAction = onAction,
      )

    is AccountLine ->
      AccountRow(
        modifier = modifier,
        account = line.account,
        handleModifier = handle,
        onClick = { onAction(Show(AccountMenu(line.account))) },
      )

    is EmptyLine -> Unit
  }
}

internal object ManageAccountsDS {
  val sectionSpacing = 16.dp
  val rowHeight = 60.dp
  val headerHeight = 44.dp
  val rowPadding = 12.dp
  val handleWidth = 44.dp
  val autoScrollEdge = 64.dp
  val autoScrollSpeed = 800.dp
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
