package aktual.budget.list.ui

import aktual.budget.list.vm.CreateBudgetState
import aktual.budget.list.vm.DeletingState
import aktual.budget.list.vm.ImportBudgetState
import aktual.budget.list.vm.ListBudgetsEvent
import aktual.budget.list.vm.ListBudgetsState
import aktual.budget.list.vm.ListBudgetsViewModel
import aktual.budget.model.Budget
import aktual.budget.model.BudgetId
import aktual.budget.model.directoryId
import aktual.budget.sync.components.SyncBudgetDialog
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.Refresh
import aktual.core.l10n.Strings
import aktual.core.model.ServerUrl
import aktual.core.nav.BudgetNavRailNavigator
import aktual.core.nav.ChangePasswordNavigator
import aktual.core.nav.InfoNavigator
import aktual.core.nav.MetricsNavigator
import aktual.core.nav.ServerUrlNavigator
import aktual.core.nav.SettingsNavigator
import aktual.core.ui.AktualFab
import aktual.core.ui.CornerFabs
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.HazedPullToRefreshBox
import aktual.core.ui.OnAction
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.WavyBackground
import aktual.core.ui.hazedTopBar
import aktual.core.ui.isScrollingUp
import aktual.core.ui.rememberFilePicker
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import java.io.InputStream
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet

@Composable
fun ListBudgetsScreen(
  toInfo: InfoNavigator,
  toChangePassword: ChangePasswordNavigator,
  toSettings: SettingsNavigator,
  toMetrics: MetricsNavigator,
  logOut: ServerUrlNavigator,
  toBudget: BudgetNavRailNavigator,
  viewModel: ListBudgetsViewModel = metroViewModel(),
) {
  val uriHandler = LocalUriHandler.current
  val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()

  var budgetToSync by remember { mutableStateOf<BudgetId?>(null) }
  var showImport by remember { mutableStateOf(false) }
  var showCreate by remember { mutableStateOf(false) }

  LaunchedEffect(viewModel.event) {
    viewModel.event.collect { event ->
      when (event) {
        NavToBudget -> {
          showImport = false
          showCreate = false
          toBudget()
        }
        ListBudgetsEvent.LogOut -> logOut()
        is ShowSyncDialog -> budgetToSync = event.id
      }
    }
  }

  var budgetToDelete by remember { mutableStateOf<Budget?>(null) }
  val deletingState by viewModel.deletingState.collectAsStateWithLifecycle()
  budgetToDelete?.let { budget ->
    DeleteBudget(
      budget = budget,
      deletingState = deletingState,
      onDeleteLocal = viewModel::deleteLocal,
      onDeleteRemote = viewModel::deleteRemote,
      onDismiss = {
        budgetToDelete = null
        viewModel.clearDeletingState()
      },
    )
  }

  LaunchedEffect(Unit) {
    viewModel.closeDialog.collect { closeDialog ->
      if (closeDialog) {
        budgetToDelete = null
      }
    }
  }

  val importState by viewModel.importState.collectAsStateWithLifecycle()
  ImportBudget(
    isOpen = showImport,
    state = importState,
    onImport = viewModel::importZip,
    onDismiss = {
      showImport = false
      viewModel.clearImportFailure()
    },
  )

  val createState by viewModel.createState.collectAsStateWithLifecycle()
  if (showCreate) {
    CreateBudget(
      state = createState,
      listState = state,
      onCreate = viewModel::createBudget,
      onOpenCreate = viewModel::openCreatedBudget,
      onDismiss = {
        showCreate = false
        viewModel.clearCreateFailure()
      },
    )
  }

  budgetToSync?.let { id ->
    SyncBudgetDialog(
      budgetId = id,
      onSyncComplete = { viewModel.onSyncComplete(id) },
      onDismissRequest = { budgetToSync = null },
    )
  }

  ListBudgetsScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        CreateBudget -> {
          viewModel.clearCreateFailure()
          showCreate = true
        }
        ImportBudget -> {
          viewModel.clearImportFailure()
          showImport = true
        }
        LogOut -> viewModel.logOut()
        ChangePassword -> toChangePassword()
        OpenAbout -> toInfo()
        OpenSettings -> toSettings()
        OpenServerMetrics -> toMetrics()
        OpenInBrowser -> serverUrl?.openIn(uriHandler)
        Reload -> viewModel.retry()
        is Delete -> budgetToDelete = action.budget
        is Open -> viewModel.openBudget(action.budget)
      }
    },
  )
}

@Composable
private fun DeleteBudget(
  budget: Budget,
  deletingState: DeletingState,
  onDeleteLocal: (BudgetId) -> Unit,
  onDeleteRemote: (Budget.Cloud) -> Unit,
  onDismiss: () -> Unit,
) {
  DeleteBudgetDialog(
    budget = budget,
    deletingState = deletingState,
    onAction = { action ->
      when (action) {
        DeleteLocal -> onDeleteLocal(budget.directoryId)
        DeleteRemote -> if (budget is Cloud) onDeleteRemote(budget)
        Dismiss -> onDismiss()
      }
    },
  )
}

@Composable
private fun CreateBudget(
  state: CreateBudgetState,
  listState: ListBudgetsState,
  onCreate: (name: String, syncWithServer: Boolean) -> Unit,
  onOpenCreate: () -> Unit,
  onDismiss: () -> Unit,
) {
  val existingNames =
    remember(listState) {
      val budgets = (listState as? Success)?.budgets.orEmpty()
      budgets.mapTo(mutableSetOf()) { it.name }.toImmutableSet()
    }
  CreateBudgetDialog(
    state = state,
    existingNames = existingNames,
    onCreate = onCreate,
    onOpenCreate = onOpenCreate,
    onDismiss = onDismiss,
  )
}

@Composable
private fun ImportBudget(
  isOpen: Boolean,
  state: ImportBudgetState,
  onImport: (open: () -> InputStream) -> Unit,
  onDismiss: () -> Unit,
) {
  val filePicker =
    rememberFilePicker(
      mimeTypes = ZIP_MIME_TYPES,
      extension = "zip",
      title = Strings.budgetImportPickerTitle,
      onPick = onImport,
    )
  if (isOpen) {
    ImportBudgetDialog(state = state, onSelectFile = filePicker::pick, onDismiss = onDismiss)
  }
}

// Some file managers label zips with the legacy or generic types
private val ZIP_MIME_TYPES =
  persistentListOf("application/zip", "application/x-zip-compressed", "application/octet-stream")

private fun ServerUrl.openIn(uriHandler: UriHandler) = uriHandler.openUri(toString())

@Composable
internal fun ListBudgetsScaffold(state: ListBudgetsState, onAction: OnAction<ListBudgetsAction>) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        title = { ScaffoldTitle() },
        actions = { TopBarActions(onAction) },
      )
    },
  ) { innerPadding ->
    Box {
      WavyBackground()

      HazedPullToRefreshBox(
        modifier = Modifier.padding(horizontal = 8.dp),
        onRefresh = { onAction(Reload) },
        isRefreshing = state is Loading,
        contentAlignment = Center,
        hazeState = hazeState,
        innerPadding = innerPadding,
      ) { padding ->
        StateContent(
          state = state,
          onAction = onAction,
          listState = listState,
          contentPadding = padding,
        )
      }

      // The empty state has its own create button
      CornerFabs(
        end = {
          AktualFab(
            text = Strings.listBudgetsCreate,
            icon = AktualIcons.Add,
            isVisible = state is Success && state.budgets.isNotEmpty(),
            isExpanded = listState.isScrollingUp(),
            onClick = { onAction(CreateBudget) },
          )
        }
      )
    }
  }
}

@Composable
private fun ScaffoldTitle() {
  Text(
    text = Strings.listBudgetsToolbar,
    maxLines = 1,
    overflow = Ellipsis,
  )
}

@Composable
private fun StateContent(
  state: ListBudgetsState,
  onAction: OnAction<ListBudgetsAction>,
  listState: LazyListState,
  contentPadding: PaddingValues,
) {
  if (state is Success && state.budgets.isNotEmpty()) {
    // LazyColumn fills the box behind the top bar, with top contentPadding so items can scroll
    // under it
    ContentSuccess(
      budgets = state.budgets,
      listState = listState,
      contentPadding = contentPadding,
      onClickOpen = { budget -> onAction(Open(budget)) },
      onClickDelete = { budget -> onAction(Delete(budget)) },
    )
  } else {
    Column(
      modifier = Modifier.fillMaxSize().padding(contentPadding),
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = CenterHorizontally,
    ) {
      when (state) {
        is Loading -> ShimmerBudgetList(state.numLoadingItems)

        is Failure ->
          FailureScreen(
            title = Strings.budgetFailureMessage,
            reason = state.reason ?: Strings.budgetFailureDefaultMessage,
            action =
              FailureAction(
                text = { Strings.budgetFailureRetry },
                onClick = { onAction(Reload) },
                icon = AktualIcons.Refresh,
              ),
          )

        is Success ->
          ContentEmpty(
            onCreateBudget = { onAction(CreateBudget) },
            onCreateBudgetInBrowser = { onAction(OpenInBrowser) },
          )
      }

      BottomSpacing()
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewListBudgetsScaffold(
  @PreviewParameter(ListBudgetsScaffoldProvider::class) params: ColorParams<ListBudgetsState>,
) = PreviewWithColorParams(params) { ListBudgetsScaffold(state = this, onAction = {}) }

private val PREVIEW_ITEMS =
  List(size = 5) { PreviewBudgetSynced } +
    List(size = 5) { PreviewBudgetRemote } +
    List(size = 5) { PreviewBudgetBroken }

private class ListBudgetsScaffoldProvider :
  ColorParamsProvider<ListBudgetsState>(
    ListBudgetsState.Success(PREVIEW_ITEMS.toImmutableList()),
    ListBudgetsState.Loading(numLoadingItems = 3),
    ListBudgetsState.Failure(reason = "Something broke lol"),
  )
