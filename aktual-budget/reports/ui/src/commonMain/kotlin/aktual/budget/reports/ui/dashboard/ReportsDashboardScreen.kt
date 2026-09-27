package aktual.budget.reports.ui.dashboard

import aktual.budget.model.DashboardPageId
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.ui.charts.PREVIEW_CASH_FLOW_DATA
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.dashboard.DashboardItem
import aktual.budget.reports.vm.dashboard.DashboardPage
import aktual.budget.reports.vm.dashboard.ReportsDashboardViewModel
import aktual.core.icons.material.Add
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.CreateReportNavigator
import aktual.core.nav.ReportNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
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
  viewModel: ReportsDashboardViewModel = metroViewModel(),
) {
  val pages by viewModel.allPages.collectAsStateWithLifecycle()
  val content by viewModel.content.collectAsStateWithLifecycle()

  ReportsDashboardScaffold(
    pages = pages,
    selectedPage = content.page,
    items = content.items,
    observer = viewModel::observeChartData,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        is OpenItem -> toReport(action.id)
        is Rename -> viewModel.renameReport(action.item, action.name)
        is Delete -> viewModel.deleteReport(action.id)
        is SetSummaryType -> TODO()
        is SetAllTimeDivisor -> TODO()
        is ClickCalendarDay -> TODO()
        is SaveTextContent -> TODO()
        CreateNewReport -> content.page?.let { page -> toCreateReport(page.id) }
        is SelectPage -> viewModel.selectPage(action.id)
        is CreatePage -> viewModel.createPage(action.name)
        is RenamePage -> viewModel.renamePage(action.id, action.name)
        is DeletePage -> viewModel.deletePage(action.id)
      }
    },
  )
}

@Composable
internal fun ReportsDashboardScaffold(
  pages: ImmutableList<DashboardPage>,
  selectedPage: DashboardPage?,
  items: ImmutableList<DashboardItem>,
  observer: DashboardItemObserver,
  onAction: ActionListener,
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberPageListState(selectedPage?.id)

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavDrawerIconButton() },
        title = { DashboardSelector(pages, selectedPage, onAction) },
        actions = {
          IconButton(onClick = { onAction(CreateNewReport) }) {
            Icon(
              imageVector = MaterialIcons.Add,
              contentDescription = Strings.reportsDashboardCreate,
            )
          }
          DashboardMenu(page = selectedPage, canDelete = pages.size > 1, onAction = onAction)
        },
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()
      ReportsDashboardContent(
        modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
        contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
        items = items,
        observer = observer,
        listState = listState,
        onAction = onAction,
      )
    }
  }
}

// Not SaveableStateProvider: its ReusableContent recycles the top bar's dropdown popup between
// pages and leaks it
@Composable
private fun rememberPageListState(page: DashboardPageId?): LazyListState {
  val states = rememberSaveable(saver = PageListStatesSaver) { mutableMapOf() }
  return remember(page) { states.getOrPut(page?.value.orEmpty()) { LazyListState() } }
}

private val PageListStatesSaver: Saver<MutableMap<String, LazyListState>, Any> =
  mapSaver(
    save = { states ->
      states.mapValues { (_, state) ->
        intArrayOf(state.firstVisibleItemIndex, state.firstVisibleItemScrollOffset)
      }
    },
    restore = { saved ->
      saved.mapValuesTo(mutableMapOf()) { (_, value) ->
        val (index, offset) = value as? IntArray ?: return@mapValuesTo LazyListState()
        LazyListState(index, offset)
      }
    },
  )

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
private fun ContentEmpty(modifier: Modifier = Modifier) =
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Center) {
    Text(text = Strings.reportsDashboardEmpty, color = colors.pageText)
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
    items(items, key = { it.id.value }) { item ->
      DashboardItem(
        modifier = Modifier.animateItem(),
        item = item,
        observer = observer,
        onAction = onAction,
      )
    }

    item { BottomSpacing() }
  }
}

@Preview
@Composable
private fun PreviewReportsDashboardScaffold(
  @PreviewParameter(ReportsDashboardScaffoldProvider::class)
  params: ColoredParams<ReportsDashboardScaffoldParams>
) =
  PreviewWithColoredParams(params) {
    ReportsDashboardScaffold(
      pages = pages,
      selectedPage = pages.firstOrNull(),
      items = items,
      observer = { if (chartData == null) flowOf() else flowOf(chartData) },
      onAction = {},
    )
  }

private data class ReportsDashboardScaffoldParams(
  val pages: ImmutableList<DashboardPage>,
  val items: ImmutableList<DashboardItem>,
  val chartData: ChartData?,
)

private class ReportsDashboardScaffoldProvider :
  ColoredParameterProvider<ReportsDashboardScaffoldParams>(
    ReportsDashboardScaffoldParams(
      pages =
        persistentListOf(
          DashboardPage(DashboardPageId("a"), "Main"),
          DashboardPage(DashboardPageId("b"), "Savings"),
        ),
      items =
        persistentListOf(
          PREVIEW_DASHBOARD_ITEM_1,
          PREVIEW_DASHBOARD_ITEM_2,
          PREVIEW_DASHBOARD_ITEM_3,
        ),
      chartData = PREVIEW_CASH_FLOW_DATA,
    ),
    ReportsDashboardScaffoldParams(
      pages = persistentListOf(),
      items = persistentListOf(),
      chartData = null,
    ),
  )
