package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.BudgetState
import aktual.budget.budgeting.vm.BudgetViewModel
import aktual.budget.budgeting.vm.category
import aktual.core.icons.material.CalendarToday
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.MoreVert
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.Visibility
import aktual.core.icons.material.VisibilityOff
import aktual.core.l10n.Strings
import aktual.core.nav.BudgetCategoryNavigator
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
import aktual.core.ui.isCompactWidth
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.topBarHazeOffset
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.datetime.YearMonth
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus

@Composable
internal fun BudgetScreen(
  month: YearMonth?,
  transactions: TransactionsNavigator,
  categories: BudgetCategoryNavigator,
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
        is ShowMonth -> viewModel.showMonth(action.month)
        PreviousMonth -> viewModel.previousMonth()
        NextMonth -> viewModel.nextMonth()
        ShowToday -> viewModel.showToday()
        is SetMonthCount -> viewModel.setMonthCount(action.count)
        is SetFittingMonths -> viewModel.setFittingMonths(action.count)
        ToggleSpent -> viewModel.toggleSpent()
        ToggleHidden -> viewModel.toggleHidden()
        is ToggleGroup -> viewModel.toggleCollapsed(action.id)
        ReviewUncategorised -> transactions.uncategorised()
        is OpenCategory -> categories(action.category, action.month)
        // The scaffold opens the sheet
        is EditBudget -> Unit
        is SetBudget -> viewModel.setBudget(action.month, action.category, action.input)
        is ApplyQuickAction -> viewModel.apply(action)
      }
    },
  )
}

private fun BudgetViewModel.apply(action: ApplyQuickAction) {
  val (month, category) = action
  when (action.action) {
    CopyLastMonth -> copyLastMonth(month, category)
    Average3 -> setAverage(month, category, months = 3)
    Average6 -> setAverage(month, category, months = 6)
    Average12 -> setAverage(month, category, months = 12)
    CopyToYearEnd -> copyToYearEnd(month, category)
  }
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
  isCompact: Boolean = isCompactWidth(),
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()
  val pageListStates = remember { PageListStates() }
  val visibleMonth by rememberUpdatedState((state as? BudgetState.Loaded)?.month)
  var showMonthPicker by remember { mutableStateOf(false) }
  var editing by remember { mutableStateOf<EditBudget?>(null) }
  val latestOnAction by rememberUpdatedState(onAction)
  val handler = remember {
    BudgetActionHandler { action ->
      if (action is EditBudget) editing = action else latestOnAction(action)
    }
  }
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(focusRequester) { focusRequester.requestFocus() }

  Scaffold(
    modifier =
      modifier
        .fillMaxSize()
        .onKeyEvent { event -> event.changeMonth(state, handler) }
        .focusRequester(focusRequester)
        .focusable(),
    topBar = {
      TopAppBar(
        modifier =
          Modifier.hazedTopBar(
            state = hazeState,
            scrollOffset = {
              val pageListState = if (isCompact) pageListStates[visibleMonth] else null
              (pageListState ?: listState).topBarHazeOffset()
            },
          ),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavDrawerIconButton() },
        title = {
          if (state is Loaded) {
            MonthSelector(
              modifier = Modifier.fillMaxWidth(),
              state = state,
              onAction = handler,
              onPickMonth = { showMonthPicker = true },
            )
          } else {
            Text(text = Strings.budgetingTitle, maxLines = 1, overflow = Ellipsis)
          }
        },
        actions = {
          if (state is Loaded) {
            if (!isCompact && state.maxMonthCount > 1) {
              MonthCountSelector(
                count = state.monthCount,
                max = state.maxMonthCount,
                onAction = handler,
              )
            }
            BudgetMenu(state = state, onAction = handler)
          }
        },
      )
    },
  ) { innerPadding ->
    BoxWithConstraints {
      val fitting = if (isCompact) 1 else fittingMonths(maxWidth)
      SideEffect(fitting) { handler(SetFittingMonths(fitting)) }

      PageBackground()

      HazedPullToRefreshBox(
        hazeState = hazeState,
        innerPadding = innerPadding,
        isRefreshing = isRefreshing,
        onRefresh = { handler(Refresh) },
      ) { padding ->
        BudgetContent(
          state = state,
          isCompact = isCompact,
          contentPadding = padding,
          listState = listState,
          pageListStates = pageListStates,
          onAction = handler,
        )
      }
    }
  }

  if (showMonthPicker && state is Loaded) {
    MonthPickerSheet(state = state, onAction = handler, onDismiss = { showMonthPicker = false })
  }

  val edit = editing
  val category = edit?.let { (state as? Loaded)?.get(it.month)?.category(it.category) }
  if (edit != null && category != null && state is Loaded) {
    BudgetSheet(
      month = edit.month,
      category = category,
      type = state.type,
      onAction = handler,
      onDismiss = { editing = null },
    )
  }
}

// Ctrl and the left or right arrow, as upstream's desktop budget
private fun KeyEvent.changeMonth(state: BudgetState, onAction: BudgetActionHandler): Boolean {
  if (state !is Loaded || type != KeyDown || !isCtrlPressed) {
    return false
  }

  return when (key) {
    DirectionLeft if state.canGoBack -> {
      onAction(PreviousMonth)
      true
    }

    DirectionRight if state.canGoForward -> {
      onAction(NextMonth)
      true
    }

    else -> {
      false
    }
  }
}

@Composable
private fun BudgetContent(
  state: BudgetState,
  isCompact: Boolean,
  contentPadding: PaddingValues,
  listState: LazyListState,
  pageListStates: PageListStates,
  onAction: BudgetActionHandler,
  modifier: Modifier = Modifier,
) {
  // Sticky headers ignore content padding, so the list starts below the top bar rather than under
  // it, or they'd hide behind it
  val listModifier = modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding())

  when (state) {
    Loading -> LoadingTable(modifier = listModifier)

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
      when {
        state.isEmpty ->
          FailureScreen(
            modifier = modifier,
            title = Strings.budgetingEmpty,
            reason = null,
            icon = null,
            background = colors.tableBackground,
            action = null,
          )

        isCompact ->
          BudgetPager(
            modifier = listModifier,
            state = state,
            pageListStates = pageListStates,
            onAction = onAction,
          )

        else ->
          BudgetColumns(
            modifier = listModifier,
            state = state,
            listState = listState,
            onAction = onAction,
          )
      }
  }
}

@Composable
private fun LoadingTable(modifier: Modifier = Modifier) =
  Column(modifier = modifier.padding(BudgetDS.listPadding)) {
    ShimmerBudgetTable()
    BottomSpacing()
  }

// Each page of the pager scrolls on its own
@Stable
private class PageListStates {
  private val states = mutableStateMapOf<YearMonth, LazyListState>()

  operator fun get(month: YearMonth?): LazyListState? = states[month]

  operator fun set(month: YearMonth, state: LazyListState?) {
    if (state == null) states -= month else states[month] = state
  }
}

// One month per page, between the budget's bounds
@Composable
private fun BudgetPager(
  state: BudgetState.Loaded,
  pageListStates: PageListStates,
  onAction: BudgetActionHandler,
  modifier: Modifier = Modifier,
) {
  val page = state.earliest.monthsUntil(state.month)
  val pagerState =
    rememberPagerState(initialPage = page) { state.earliest.monthsUntil(state.latest) + 1 }
  val shownPage by rememberUpdatedState(page)
  val earliest by rememberUpdatedState(state.earliest)

  // The arrows, the month picker and Today move the pager
  LaunchedEffect(page) {
    if (pagerState.targetPage != page) pagerState.animateScrollToPage(page)
  }

  // And swiping moves the month
  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.settledPage }
      .collect { settled ->
        if (settled != shownPage) onAction(ShowMonth(earliest.plus(settled, MONTH)))
      }
  }

  HorizontalPager(modifier = modifier, state = pagerState, key = { it }) { index ->
    val month = state.earliest.plus(index, MONTH)
    val budget = state[month]
    val listState = rememberLazyListState()

    DisposableEffect(month, listState) {
      pageListStates[month] = listState
      onDispose { pageListStates[month] = null }
    }

    if (budget == null) {
      LoadingTable(modifier = Modifier.fillMaxSize())
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize().scrollbar(listState),
        state = listState,
        contentPadding = BudgetDS.listPadding,
      ) {
        budgetTable(budget, state.type, state.showSpent, onAction)
        item(key = "bottom") { BottomSpacing() }
      }
    }
  }
}

@Composable
private fun BudgetMenu(
  state: BudgetState.Loaded,
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
      if (state.month != state.current) {
        AktualDropdownMenuItem(
          text = Strings.budgetingToday,
          leadingIcon = MaterialIcons.CalendarToday,
          onClick = {
            expanded = false
            onAction(ShowToday)
          },
        )
      }

      val showHidden = state.showHidden
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
    BudgetScaffold(state = this, isRefreshing = false, onAction = {}, isCompact = true)
  }

private class BudgetColumnsProvider :
  ColoredParameterProvider<BudgetState>(
    previewColumns(count = 1),
    previewColumns(count = 2),
    previewColumns(count = 4),
    previewColumns(count = 3, type = Tracking),
  )

@Preview(widthDp = 1440, heightDp = 900, locale = "en")
@Composable
private fun PreviewBudgetColumns(
  @PreviewParameter(BudgetColumnsProvider::class) params: ColoredParams<BudgetState>,
) =
  PreviewWithColoredParams(params) {
    BudgetScaffold(state = this, isRefreshing = false, onAction = {}, isCompact = false)
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
