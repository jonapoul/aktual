package aktual.budget.payees.ui.list

import aktual.budget.model.PayeeId
import aktual.budget.payees.ui.openRules
import aktual.budget.payees.vm.list.Empty
import aktual.budget.payees.vm.list.Failure
import aktual.budget.payees.vm.list.ListPayeesState
import aktual.budget.payees.vm.list.ListPayeesViewModel
import aktual.budget.payees.vm.list.Loading
import aktual.budget.payees.vm.list.PayeeItem
import aktual.budget.payees.vm.list.Success
import aktual.core.icons.AktualIcons
import aktual.core.icons.DotsHorizontalTriple
import aktual.core.icons.LightBulb
import aktual.core.icons.Refresh
import aktual.core.icons.SearchAlternate
import aktual.core.l10n.Strings
import aktual.core.nav.EditPayeeNavigator
import aktual.core.nav.EditRuleNavigator
import aktual.core.nav.SearchPayeesNavigator
import aktual.core.nav.SearchRulesNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.ColorParams
import aktual.core.ui.Dimens
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.ListBottomSheet
import aktual.core.ui.LocalSnackbars
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun ListPayeesScreen(
  toSearch: SearchPayeesNavigator,
  toEdit: EditPayeeNavigator,
  editRule: EditRuleNavigator,
  searchRules: SearchRulesNavigator,
  modifier: Modifier = Modifier,
  viewModel: ListPayeesViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbars = LocalSnackbars.current

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        is Deleted -> snackbars.show { showDeleted(event, onUndo = viewModel::undoDelete) }
        is Merged -> snackbars.show { showMerged(event, onUndo = viewModel::undoMerge) }
        DeleteFailed,
        MergeFailed,
        RestoreFailed,
        UpdateFailed -> snackbars.show { showFailure(event) }
      }
    }
  }

  ListPayeesScaffold(
    modifier = modifier,
    state = state,
    onAction = { action ->
      when (action) {
        Retry -> viewModel.retry()
        OpenSearch -> toSearch()
        ToggleUnusedOnly -> viewModel.toggleUnusedOnly()
        OpenCategoryLearning -> Unit
        is SetLearningEnabled -> viewModel.setLearningEnabled(action.enabled)
        is OpenPayee -> toEdit(action.id)
        is OpenRules -> openRules(action.payee, editRule, searchRules)
        is Delete -> viewModel.delete(action.payee)
        is StartMerge -> Unit
        is Merge -> viewModel.merge(action.payee, action.into)
        is ToggleFavorite -> viewModel.toggleFavorite(action.payee)
      }
    },
  )
}

@Composable
private fun ListPayeesScaffold(
  state: ListPayeesState,
  onAction: ListPayeesActionHandler,
  modifier: Modifier = Modifier,
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()
  var confirmDelete by remember { mutableStateOf<PayeeItem?>(null) }
  var mergeSource by remember { mutableStateOf<PayeeItem?>(null) }
  var showCategoryLearning by remember { mutableStateOf(false) }

  // Only worth confirming when transactions will lose their payee
  val handleAction = ListPayeesActionHandler { action ->
    @Suppress("ElseCaseInsteadOfExhaustiveWhen")
    when (action) {
      is Delete if action.payee.numTransactions > 0 -> confirmDelete = action.payee
      is StartMerge -> mergeSource = action.payee
      OpenCategoryLearning -> showCategoryLearning = true
      else -> onAction(action)
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavDrawerIconButton() },
        title = { Text(text = Strings.payeesTitle) },
        actions = {
          if (state is Success) {
            BareIconButton(
              imageVector = AktualIcons.SearchAlternate,
              contentDescription = Strings.payeesSearch,
              onClick = { onAction(OpenSearch) },
            )
            ListPayeesMenu(onAction = handleAction)
          }
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      Box(
        modifier =
          Modifier.padding(horizontal = Dimens.ScreenPadding)
            .hazedTopBarContent(hazeState, innerPadding),
        contentAlignment = Center,
      ) {
        ListPayeesContent(
          state = state,
          onAction = handleAction,
          listState = listState,
          padding = hazedTopBarContentPadding(hazeState, innerPadding),
        )
      }

      confirmDelete?.let { payee ->
        DeletePayeeDialog(
          name = payee.name,
          onConfirm = {
            confirmDelete = null
            onAction(Delete(payee))
          },
          onCancel = { confirmDelete = null },
        )
      }

      if (showCategoryLearning && state is Success) {
        CategoryLearningDialog(
          isEnabled = state.isLearningEnabled,
          onEnabledChange = { onAction(SetLearningEnabled(it)) },
          onDismiss = { showCategoryLearning = false },
        )
      }

      val source = mergeSource
      if (source != null && state is Success) {
        MergePayeeSheet(
          payee = source,
          payees = state.payees,
          onSelect = { target -> onAction(Merge(source, target)) },
          onDismiss = { mergeSource = null },
        )
      }
    }
  }
}

@Composable
private fun ListPayeesMenu(onAction: ListPayeesActionHandler, modifier: Modifier = Modifier) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    BareIconButton(
      imageVector = AktualIcons.DotsHorizontalTriple,
      contentDescription = Strings.payeesMenu,
      onClick = { expanded = true },
    )

    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      AktualDropdownMenuItem(
        text = Strings.payeesCategoryLearning,
        leadingIcon = AktualIcons.LightBulb,
        onClick = {
          expanded = false
          onAction(OpenCategoryLearning)
        },
      )
    }
  }
}

@Composable
private fun DeletePayeeDialog(name: String, onConfirm: () -> Unit, onCancel: () -> Unit) {
  AktualAlertDialog(
    title = Strings.payeesDeleteConfirmTitle(name),
    highlight = colors.errorText,
    onDismissRequest = onCancel,
    buttons = {
      TextButton(onClick = onCancel) { Text(Strings.payeesDeleteCancel) }
      TextButton(onClick = onConfirm) {
        Text(Strings.payeesDeleteConfirm, color = colors.errorText)
      }
    },
    content = { Text(Strings.payeesDeleteConfirmMessage) },
  )
}

// Transfer payees can't be merged into, as upstream
@Composable
private fun MergePayeeSheet(
  payee: PayeeItem,
  payees: ImmutableList<PayeeItem>,
  onSelect: (PayeeItem) -> Unit,
  onDismiss: () -> Unit,
) {
  val targets =
    remember(payee, payees) {
      payees.filter { !it.isTransfer && it.id != payee.id }.toImmutableList()
    }
  ListBottomSheet(
    value = payee,
    options = targets,
    onDismiss = onDismiss,
    onSelect = onSelect,
    sheetState = rememberBottomSheetState(initialValue = Hidden),
    string = { it.name },
    key = { it.id.value },
    searchPlaceholder = Strings.payeesMergePlaceholder(payee.name),
  )
}

@Composable
private fun ListPayeesContent(
  state: ListPayeesState,
  onAction: ListPayeesActionHandler,
  listState: LazyListState,
  padding: PaddingValues,
) {
  when (state) {
    Loading ->
      Column(
        modifier = Modifier.fillMaxSize().padding(padding),
        verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
      ) {
        repeat(ListPayeesDS.SHIMMER_ITEMS) { ShimmerPayeeItem() }
      }

    is Failure ->
      FailureScreen(
        title = Strings.payeesFailurePrefix,
        reason = state.cause ?: Strings.payeesFailureDefaultMessage,
        background = colors.tableBackground,
        action =
          FailureAction(
            text = { Strings.syncRetry },
            icon = AktualIcons.Refresh,
            onClick = { onAction(Retry) },
          ),
      )

    Empty ->
      FailureScreen(
        title = Strings.payeesEmpty,
        reason = null,
        action = null,
        icon = null,
        background = colors.tableBackground,
      )

    is Success ->
      PayeesList(
        payees = state.shown,
        numUnused = state.numUnused,
        unusedOnly = state.unusedOnly,
        showLearningOff = state.isLearningEnabled,
        listState = listState,
        contentPadding = padding,
        onAction = onAction,
      )
  }
}

@Composable
private fun PayeesList(
  payees: ImmutableList<PayeeItem>,
  numUnused: Int,
  unusedOnly: Boolean,
  showLearningOff: Boolean,
  listState: LazyListState,
  contentPadding: PaddingValues,
  onAction: ListPayeesActionHandler,
  modifier: Modifier = Modifier,
) {
  // only one row may be swiped open at a time - opening another closes the previous one
  var openId by remember { mutableStateOf<PayeeId?>(null) }

  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
  ) {
    // Upstream only offers the filter when there's something to filter, or it's already on
    if (numUnused > 0 || unusedOnly) {
      item(key = "unused") {
        UnusedPayeesChip(
          numUnused = numUnused,
          isSelected = unusedOnly,
          onClick = { onAction(ToggleUnusedOnly) },
        )
      }
    }

    items(payees, key = { it.id.value }) { payee ->
      SwipeablePayeeItem(
        modifier = Modifier.animateItem(),
        payee = payee,
        isOpen = openId == payee.id,
        onOpenChange = { open ->
          openId =
            when {
              open -> payee.id
              openId == payee.id -> null
              else -> openId
            }
        },
        onAction = onAction,
        showLearningOff = showLearningOff,
      )
    }

    item { BottomSpacing() }
  }
}

private class ListPayeesStateProvider :
  ColorParamsProvider<ListPayeesState>(
    Success(PayeesPreview.all),
    Success(PayeesPreview.all, unusedOnly = true),
    Empty,
    Loading,
    Failure("Database connection lost"),
  )

@PortraitPreview
@Composable
private fun PreviewListPayeesScaffold(
    @PreviewParameter(ListPayeesStateProvider::class) params: ColorParams<ListPayeesState>,
) = PreviewWithColorParams(params) { ListPayeesScaffold(state = this, onAction = {}) }
