package aktual.budget.reports.ui

import aktual.budget.model.BudgetId
import aktual.budget.reports.vm.DashboardState
import aktual.budget.reports.vm.ReportDashboardItem
import aktual.budget.reports.vm.ReportsDashboardViewModel
import aktual.core.model.Token
import aktual.core.ui.BackHandler
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.WavyBackground
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.l10n.Strings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ReportsDashboardScreen(
  nav: ReportsDashboardNavigator,
  budgetId: BudgetId,
  token: Token,
  viewModel: ReportsDashboardViewModel = metroViewModel(token, budgetId),
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
        is Action.SaveTextContent -> TODO()
        Action.CreateNewReport -> nav.createReport(token, budgetId)
      }
    },
  )
}

@Composable
private fun metroViewModel(
  token: Token,
  budgetId: BudgetId,
) = assistedMetroViewModel<ReportsDashboardViewModel, ReportsDashboardViewModel.Factory> {
  create(token, budgetId)
}

@Composable
internal fun ReportsDashboardScaffold(
  state: DashboardState,
  onAction: ActionListener,
  theme: Theme = LocalTheme.current,
) {
  Scaffold(
    topBar = {
      TopAppBar(
        colors = theme.transparentTopAppBarColors(),
        title = { Text(Strings.reportsDashboardTitle) },
        actions = {
          IconButton(onClick = { onAction(Action.CreateNewReport) }) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = Strings.reportsDashboardCreate,
            )
          }
        },
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
  LazyColumn(
    state = listState,
    modifier = modifier
      .fillMaxWidth()
      .padding(5.dp)
      .scrollbar(listState),
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

@Preview
@Composable
private fun PreviewReportDashboardItem(
  @PreviewParameter(DashboardStateProvider::class) params: ThemedParams<DashboardState>,
) = PreviewWithColorScheme(schemeType = params.type) {
  ReportsDashboardScaffold(
    state = params.data,
    onAction = {},
  )
}

private class DashboardStateProvider : ThemedParameterProvider<DashboardState>(
  DashboardState.Loaded(
    items = persistentListOf(PREVIEW_DASHBOARD_ITEM_1, PREVIEW_DASHBOARD_ITEM_2, PREVIEW_DASHBOARD_ITEM_3),
  ),
  DashboardState.Loading,
  DashboardState.Empty,
)
