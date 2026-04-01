package aktual.budget.reports.ui.dashboard

import aktual.app.nav.BackNavigator
import aktual.app.nav.CreateReportNavigator
import aktual.app.nav.ReportNavigator
import aktual.budget.model.BudgetId
import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.ui.charts.PREVIEW_CASH_FLOW_DATA
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.dashboard.DashboardItem
import aktual.budget.reports.vm.dashboard.ReportsDashboardViewModel
import aktual.core.icons.material.Add
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.model.Token
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.PageBackground
import aktual.core.ui.PreviewWithThemedParams
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.blurredTopBarContentPadding
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf

@Composable
fun ReportsDashboardScreen(
  back: BackNavigator,
  toReport: ReportNavigator,
  toCreateReport: CreateReportNavigator,
  budgetId: BudgetId,
  token: Token,
  viewModel: ReportsDashboardViewModel = metroViewModel(),
) {
  val items by viewModel.items.collectAsStateWithLifecycle()

  ReportsDashboardScaffold(
    items = items,
    observer = viewModel::observeChartData,
    onAction = { action ->
      when (action) {
        Action.NavBack -> back()
        is Action.OpenItem -> toReport(token, budgetId, action.id)
        is Action.Rename -> viewModel.renameReport(action.item, action.name)
        is Action.Delete -> viewModel.deleteReport(action.id)
        is Action.SetSummaryType -> TODO()
        is Action.SetAllTimeDivisor -> TODO()
        is Action.ClickCalendarDay -> TODO()
        is Action.SaveTextContent -> TODO()
        Action.CreateNewReport -> toCreateReport(token, budgetId)
      }
    },
  )
}

@Composable
internal fun ReportsDashboardScaffold(
  items: ImmutableList<DashboardItem>,
  observer: DashboardItemObserver,
  onAction: ActionListener,
  theme: Theme = LocalTheme.current,
) {
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, isScrolled = listState.canScrollBackward),
        colors = theme.transparentTopAppBarColors(),
        title = { Text(Strings.reportsDashboardTitle) },
        actions = {
          IconButton(onClick = { onAction(Action.CreateNewReport) }) {
            Icon(
              imageVector = MaterialIcons.Add,
              contentDescription = Strings.reportsDashboardCreate,
            )
          }
        },
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()
      ReportsDashboardContent(
        modifier = Modifier.blurredTopBarContent(blurState, innerPadding),
        contentPadding = blurredTopBarContentPadding(blurState, innerPadding),
        items = items,
        observer = observer,
        listState = listState,
        onAction = onAction,
      )
    }
  }
}

@Composable
private fun ReportsDashboardContent(
  items: ImmutableList<DashboardItem>,
  observer: DashboardItemObserver,
  listState: LazyListState,
  onAction: ActionListener,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  if (items.isEmpty()) {
    ContentEmpty(modifier)
  } else {
    ContentList(
      items = items,
      observer = observer,
      listState = listState,
      onAction = onAction,
      contentPadding = contentPadding,
      modifier = modifier,
    )
  }
}

@Composable
private fun ContentEmpty(modifier: Modifier = Modifier, theme: Theme = LocalTheme.current) =
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = Strings.reportsDashboardEmpty, color = theme.pageText)
  }

@Composable
private fun ContentList(
  items: ImmutableList<DashboardItem>,
  observer: DashboardItemObserver,
  listState: LazyListState,
  onAction: ActionListener,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.scrollbar(listState).padding(4.dp),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    items(items) { item -> DashboardItem(item, observer, onAction) }

    item {
      BottomStatusBarSpacing()
      BottomNavBarSpacing()
    }
  }
}

@Preview
@Composable
private fun PreviewReportsDashboardScaffold(
  @PreviewParameter(ReportsDashboardScaffoldProvider::class)
  params: ThemedParams<ReportsDashboardScaffoldParams>
) =
  PreviewWithThemedParams(params) {
    ReportsDashboardScaffold(
      items = items,
      observer = { if (chartData == null) flowOf() else flowOf(chartData) },
      onAction = {},
    )
  }

private data class ReportsDashboardScaffoldParams(
  val items: ImmutableList<DashboardItem>,
  val chartData: ChartData?,
)

private class ReportsDashboardScaffoldProvider :
  ThemedParameterProvider<ReportsDashboardScaffoldParams>(
    ReportsDashboardScaffoldParams(
      items =
        persistentListOf(
          PREVIEW_DASHBOARD_ITEM_1,
          PREVIEW_DASHBOARD_ITEM_2,
          PREVIEW_DASHBOARD_ITEM_3,
        ),
      chartData = PREVIEW_CASH_FLOW_DATA,
    ),
    ReportsDashboardScaffoldParams(items = persistentListOf(), chartData = null),
  )
