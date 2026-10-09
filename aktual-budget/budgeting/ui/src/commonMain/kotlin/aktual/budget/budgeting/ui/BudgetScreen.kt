package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.BudgetState
import aktual.budget.budgeting.vm.BudgetViewModel
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.MoreVert
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.Visibility
import aktual.core.icons.material.VisibilityOff
import aktual.core.l10n.Strings
import aktual.core.nav.TransactionsNavigator
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.HazedPullToRefreshBox
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.hazedTopBar
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.stringLong
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.datetime.YearMonth

@Composable
internal fun BudgetScreen(
  month: YearMonth?,
  transactions: TransactionsNavigator,
  modifier: Modifier = Modifier,
  viewModel: BudgetViewModel = budgetViewModel(month),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

  BudgetScaffold(
    modifier = modifier,
    state = state,
    isRefreshing = isSyncing,
    onAction = { action ->
      when (action) {
        Refresh -> viewModel.refresh()
        Retry -> viewModel.retry()
        ToggleSpent -> viewModel.toggleSpent()
        ToggleHidden -> viewModel.toggleHidden()
        is ToggleGroup -> viewModel.toggleCollapsed(action.id)
        ReviewUncategorised -> transactions.uncategorised()
      }
    },
  )
}

@Composable
private fun budgetViewModel(month: YearMonth?) =
  assistedMetroViewModel<BudgetViewModel, BudgetViewModel.Factory>(key = month.toString()) {
    create(month)
  }

@Composable
private fun BudgetScaffold(
  state: BudgetState,
  isRefreshing: Boolean,
  onAction: BudgetActionHandler,
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
        navigationIcon = { NavDrawerIconButton() },
        title = {
          Text(
            text = (state as? BudgetState.Loaded)?.month?.stringLong() ?: Strings.budgetingTitle,
            maxLines = 1,
            overflow = Ellipsis,
          )
        },
        actions = {
          if (state is Loaded) {
            BudgetMenu(showHidden = state.showHidden, onAction = onAction)
          }
        },
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()

      HazedPullToRefreshBox(
        hazeState = hazeState,
        innerPadding = innerPadding,
        isRefreshing = isRefreshing,
        onRefresh = { onAction(Refresh) },
      ) { padding ->
        BudgetContent(
          state = state,
          contentPadding = padding,
          listState = listState,
          onAction = onAction,
        )
      }
    }
  }
}

@Composable
private fun BudgetContent(
  state: BudgetState,
  contentPadding: PaddingValues,
  listState: LazyListState,
  onAction: BudgetActionHandler,
  modifier: Modifier = Modifier,
) {
  // Sticky headers ignore content padding, so the list starts below the top bar rather than under
  // it, or they'd hide behind it
  val listModifier = modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding())

  when (state) {
    Loading ->
      Column(modifier = listModifier.padding(BudgetDS.listPadding)) {
        ShimmerBudgetTable()
        BottomSpacing()
      }

    Failed ->
      FailureScreen(
        modifier = modifier,
        title = Strings.budgetingFailureTitle,
        reason = Strings.budgetingFailureMessage,
        background = colors.tableBackground,
        action =
          FailureAction(
            text = { Strings.syncRetry },
            icon = MaterialIcons.Refresh,
            onClick = { onAction(Retry) },
          ),
      )

    is Loaded ->
      if (state.isEmpty) {
        FailureScreen(
          modifier = modifier,
          title = Strings.budgetingEmpty,
          reason = null,
          icon = null,
          background = colors.tableBackground,
          action = null,
        )
      } else {
        LazyColumn(
          modifier = listModifier.scrollbar(listState),
          state = listState,
          contentPadding = BudgetDS.listPadding,
        ) {
          budgetTable(state, onAction)
          item(key = "bottom") { BottomSpacing() }
        }
      }
  }
}

@Composable
private fun BudgetMenu(
  showHidden: Boolean,
  onAction: BudgetActionHandler,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    BareIconButton(
      imageVector = MaterialIcons.MoreVert,
      contentDescription = Strings.budgetingMenu,
      onClick = { expanded = true },
    )

    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      AktualDropdownMenuItem(
        text = if (showHidden) Strings.budgetingHideHidden else Strings.budgetingShowHidden,
        leadingIcon = if (showHidden) MaterialIcons.VisibilityOff else MaterialIcons.Visibility,
        onClick = {
          expanded = false
          onAction(ToggleHidden)
        },
      )
    }
  }
}

private class BudgetStateProvider :
  ColoredParameterProvider<BudgetState>(
    PREVIEW_ENVELOPE,
    PREVIEW_TRACKING,
    PREVIEW_EMPTY,
    BudgetState.Loading,
    BudgetState.Failed,
  )

@PortraitPreview
@Composable
private fun PreviewBudgetScaffold(
  @PreviewParameter(BudgetStateProvider::class) params: ColoredParams<BudgetState>,
) =
  PreviewWithColoredParams(params) {
    BudgetScaffold(state = this, isRefreshing = false, onAction = {})
  }

internal object BudgetDS {
  val listPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp)
  val valueWidth = 104.dp
  val balanceWidth = 96.dp
  val rowHeight = 44.dp
  val headerHeight = 36.dp
  val rowPadding = 12.dp
  val categoryIndent = 32.dp
}
