package actual.budget.reports.ui

import actual.account.model.LoginToken
import actual.budget.model.BudgetId
import actual.budget.reports.ui.ReportDashboardItems.ITEM_1
import actual.budget.reports.ui.ReportDashboardItems.ITEM_2
import actual.budget.reports.ui.ReportDashboardItems.ITEM_3
import actual.budget.reports.vm.DashboardState
import actual.budget.reports.vm.ReportDashboardItem
import actual.budget.reports.vm.ReportsDashboardViewModel
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.core.ui.WavyBackground
import actual.core.ui.transparentTopAppBarColors
import actual.l10n.Strings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

@Composable
fun ReportsDashboardScreen(
  nav: ReportsDashboardNavigator,
  budgetId: BudgetId,
  token: LoginToken,
  viewModel: ReportsDashboardViewModel = hiltViewModel(token, budgetId),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  BackHandler { nav.back() }

  ReportsDashboardScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        is Action.OpenItem -> nav.toReport(token, budgetId, action.id)
        is Action.Rename -> viewModel.renameReport(action.id)
        is Action.Delete -> viewModel.deleteReport(action.id)
        is Action.SetSummaryType -> TODO()
        is Action.SetAllTimeDivisor -> TODO()
        is Action.ClickCalendarDay -> TODO()
      }
    },
  )
}

@Composable
private fun hiltViewModel(
  token: LoginToken,
  budgetId: BudgetId,
) = hiltViewModel<ReportsDashboardViewModel, ReportsDashboardViewModel.Factory>(
  creationCallback = { factory -> factory.create(ReportsDashboardViewModel.Inputs(token, budgetId)) },
)

@Composable
private fun ReportsDashboardScaffold(
  state: DashboardState,
  onAction: ActionListener,
  theme: Theme = LocalTheme.current,
) {
  Scaffold(
    topBar = {
      TopAppBar(
        colors = theme.transparentTopAppBarColors(),
        title = { Text(Strings.reportsDashboardTitle) },
      )
    },
  ) { innerPadding ->
    Box {
      WavyBackground()

      Content(
        modifier = Modifier.padding(innerPadding),
        state = state,
        onAction = onAction,
        theme = theme,
      )
    }
  }
}

@Stable
@Composable
private fun Content(
  state: DashboardState,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  when (state) {
    DashboardState.Loading -> ContentLoading(modifier, theme)
    DashboardState.Empty -> ContentEmpty(modifier, theme)
    is DashboardState.Loaded -> ContentList(state.items, onAction, modifier, theme)
  }
}

@Composable
private fun ContentLoading(
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Box(
  modifier = modifier.fillMaxSize(),
  contentAlignment = Alignment.Center,
) {
  CircularProgressIndicator(
    modifier = Modifier.size(50.dp),
    color = theme.buttonPrimaryBackground,
  )
}

@Composable
private fun ContentEmpty(
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Box(
  modifier = modifier.fillMaxSize(),
  contentAlignment = Alignment.Center,
) {
  Text(
    text = Strings.reportsDashboardEmpty,
    color = theme.pageText,
  )
}

@Composable
private fun ContentList(
  items: ImmutableList<ReportDashboardItem>,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val listState = rememberLazyListState()
  LazyColumnScrollbar(
    state = listState,
    settings = ScrollbarSettings.Default,
  ) {
    LazyColumn(
      state = listState,
      modifier = modifier
        .fillMaxWidth()
        .padding(5.dp),
    ) {
      items(items) { item ->
        ReportDashboardItem(
          item = item,
          onAction = onAction,
          theme = theme,
        )
      }
    }
  }
}

@ScreenPreview
@Composable
private fun PreviewLoaded() = PreviewScreen {
  ReportsDashboardScaffold(
    state = DashboardState.Loaded(
      items = persistentListOf(ITEM_1, ITEM_2, ITEM_3),
    ),
    onAction = {},
  )
}

@ScreenPreview
@Composable
private fun PreviewLoading() = PreviewScreen {
  ReportsDashboardScaffold(
    state = DashboardState.Loading,
    onAction = {},
  )
}

@ScreenPreview
@Composable
private fun PreviewEmpty() = PreviewScreen {
  ReportsDashboardScaffold(
    state = DashboardState.Empty,
    onAction = {},
  )
}
