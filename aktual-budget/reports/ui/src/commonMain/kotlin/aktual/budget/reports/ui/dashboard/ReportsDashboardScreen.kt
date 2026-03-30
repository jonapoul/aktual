package aktual.budget.reports.ui.dashboard

import aktual.app.nav.BackNavigator
import aktual.app.nav.CreateReportNavigator
import aktual.app.nav.ReportNavigator
import aktual.budget.model.BudgetId
import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.vm.dashboard.DashboardState
import aktual.budget.reports.vm.dashboard.ReportDashboardItem
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
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.blurredTopBarContentPadding
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
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

@Composable
fun ReportsDashboardScreen(
  back: BackNavigator,
  toReport: ReportNavigator,
  toCreateReport: CreateReportNavigator,
  budgetId: BudgetId,
  token: Token,
  viewModel: ReportsDashboardViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  ReportsDashboardScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        Action.NavBack -> back()
        is Action.OpenItem -> toReport(token, budgetId, action.id)
        is Action.Rename -> viewModel.renameReport(action.id)
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
  state: DashboardState,
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
        state = state,
        listState = listState,
        onAction = onAction,
        theme = theme,
      )
    }
  }
}

@Composable
private fun ReportsDashboardContent(
  state: DashboardState,
  listState: LazyListState,
  onAction: ActionListener,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  when (state) {
    DashboardState.Loading -> ContentLoading(modifier, theme)
    DashboardState.Empty -> ContentEmpty(modifier, theme)
    is DashboardState.Loaded ->
      ContentList(state.items, listState, onAction, contentPadding, modifier, theme)
  }
}

@Composable
private fun ContentLoading(modifier: Modifier = Modifier, theme: Theme = LocalTheme.current) =
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    CircularProgressIndicator(
      modifier = Modifier.size(50.dp),
      color = theme.buttonPrimaryBackground,
    )
  }

@Composable
private fun ContentEmpty(modifier: Modifier = Modifier, theme: Theme = LocalTheme.current) =
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = Strings.reportsDashboardEmpty, color = theme.pageText)
  }

@Composable
private fun ContentList(
  items: ImmutableList<ReportDashboardItem>,
  listState: LazyListState,
  onAction: ActionListener,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  LazyColumn(
    state = listState,
    contentPadding = contentPadding,
    modifier = modifier.scrollbar(listState),
  ) {
    items(items) { item -> ReportDashboardItem(item = item, onAction = onAction, theme = theme) }

    item {
      BottomStatusBarSpacing()
      BottomNavBarSpacing()
    }
  }
}

@Preview
@Composable
private fun PreviewReportDashboardItem(
  @PreviewParameter(DashboardStateProvider::class) params: ThemedParams<DashboardState>
) =
  PreviewWithTheme(theme = params.theme) {
    ReportsDashboardScaffold(state = params.data, onAction = {})
  }

private class DashboardStateProvider :
  ThemedParameterProvider<DashboardState>(
    DashboardState.Loaded(
      items =
        persistentListOf(
          PREVIEW_DASHBOARD_ITEM_1,
          PREVIEW_DASHBOARD_ITEM_2,
          PREVIEW_DASHBOARD_ITEM_3,
        )
    ),
    DashboardState.Loading,
    DashboardState.Empty,
  )
