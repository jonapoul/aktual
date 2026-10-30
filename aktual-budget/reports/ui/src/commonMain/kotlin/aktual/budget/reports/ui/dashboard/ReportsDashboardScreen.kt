package aktual.budget.reports.ui.dashboard

import aktual.budget.model.CustomReportId
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.reports.ui.Action.Reorder
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.ui.charts.PREVIEW_CASH_FLOW_DATA
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.dashboard.DashboardItem
import aktual.budget.reports.vm.dashboard.DashboardPage
import aktual.budget.reports.vm.dashboard.ReportsDashboardViewModel
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.Check
import aktual.core.icons.Delete
import aktual.core.icons.SearchAlternate
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.CreateReportNavigator
import aktual.core.nav.ReportNavigator
import aktual.core.nav.SearchReportsNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BackHandler
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens
import aktual.core.ui.LocalSnackbars
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.wiggle
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.flowOf

@Composable
fun ReportsDashboardScreen(
  back: BackNavigator,
  toReport: ReportNavigator,
  toCreateReport: CreateReportNavigator,
  toSearch: SearchReportsNavigator,
  viewModel: ReportsDashboardViewModel = metroViewModel(),
) {
  val pages by viewModel.allPages.collectAsStateWithLifecycle()
  val content by viewModel.content.collectAsStateWithLifecycle()
  val pinnedIds by viewModel.pinnedIds.collectAsStateWithLifecycle()
  val canPin by viewModel.canPin.collectAsStateWithLifecycle()
  val customReportNames by viewModel.customReportNames.collectAsStateWithLifecycle()
  val snackbars = LocalSnackbars.current
  val pinnedMessage = Strings.reportsDashboardPinned

  LaunchedEffect(viewModel) { viewModel.createReportEvents.collect(toCreateReport::invoke) }

  fun pin(id: WidgetId) {
    viewModel.pinReport(id)
    snackbars.show(key = PINNED_SNACKBAR, message = pinnedMessage)
  }

  ReportsDashboardScaffold(
    pages = pages,
    selectedPage = content.page,
    items = content.items,
    pinnedIds = pinnedIds,
    canPin = canPin,
    customReportNames = customReportNames,
    observer = viewModel::observeChartData,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        is OpenItem -> toReport(action.id)
        is Rename -> viewModel.renameReport(action.item, action.name)
        is Delete -> viewModel.deleteReport(action.id)
        is DeleteSavedReport -> viewModel.deleteSavedReport(action.item)
        is Pin -> pin(action.id)
        is Unpin -> viewModel.unpinReport(action.id)
        // Text, summary options, calendar days and custom report drilldowns are only reachable
        // from the report screen
        is ClickCalendarDay,
        is SetSummaryType,
        is SetAllTimeDivisor,
        is SaveText,
        is SetUnsavedText,
        is DrillDown -> Unit
        CreateNewReport -> viewModel.createReport()
        OpenSearch -> toSearch()
        is SelectPage -> viewModel.selectPage(action.id)
        is CreatePage -> viewModel.createPage(action.name)
        is RenamePage -> viewModel.renamePage(action.id, action.name)
        is DeletePage -> viewModel.deletePage(action.id)
        is Reorder -> viewModel.reorderReports(action.order)
      }
    },
  )
}

@Composable
internal fun ReportsDashboardScaffold(
  pages: ImmutableList<DashboardPage>,
  selectedPage: DashboardPage?,
  items: ImmutableList<DashboardItem>?,
  pinnedIds: ImmutableSet<WidgetId>,
  canPin: Boolean,
  observer: DashboardItemObserver,
  onAction: ActionListener,
  customReportNames: ImmutableMap<CustomReportId, String> = persistentMapOf(),
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberPageListState(selectedPage?.id)
  val reorder = rememberReorderState(listState, items)
  var showSavePrompt by rememberSaveable { mutableStateOf(false) }

  fun save() {
    reorder.finishEditing()?.let { order -> onAction(Reorder(order)) }
  }

  fun stopEditing() {
    if (reorder.hasChanges) showSavePrompt = true else reorder.discard()
  }

  BackHandler(enabled = reorder.isEditing) { stopEditing() }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = {
          if (reorder.isEditing) {
            IconButton(onClick = ::stopEditing) {
              Icon(
                imageVector = AktualIcons.Delete,
                contentDescription = Strings.reportsDashboardReorderCancel,
              )
            }
          } else {
            NavDrawerIconButton()
          }
        },
        title = {
          if (reorder.isEditing) {
            Text(text = Strings.reportsDashboardReorder, color = colors.pageText)
          } else {
            DashboardSelector(pages, selectedPage, onAction)
          }
        },
        actions = {
          if (reorder.isEditing) {
            IconButton(onClick = ::save) {
              Icon(
                imageVector = AktualIcons.Check,
                contentDescription = Strings.reportsDashboardReorderDone,
              )
            }
            return@TopAppBar
          }

          IconButton(onClick = { onAction(OpenSearch) }) {
            Icon(
              imageVector = AktualIcons.SearchAlternate,
              contentDescription = Strings.reportsDashboardSearch,
            )
          }
          if (!items.isNullOrEmpty()) {
            IconButton(onClick = { onAction(CreateNewReport) }) {
              Icon(
                imageVector = AktualIcons.Add,
                contentDescription = Strings.reportsDashboardCreate,
              )
            }
          }
          DashboardMenu(
            page = selectedPage,
            canDelete = pages.size > 1,
            canReorder = items.orEmpty().size > 1,
            onReorder = reorder::startEditing,
            onAction = onAction,
          )
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
        pinnedIds = pinnedIds,
        canPin = canPin,
        customReportNames = customReportNames,
        observer = observer,
        listState = listState,
        reorder = reorder,
        onAction = onAction,
      )
    }
  }

  if (showSavePrompt) {
    SaveOrderDialog(
      onSave = {
        showSavePrompt = false
        save()
      },
      onDiscard = {
        showSavePrompt = false
        reorder.discard()
      },
      onCancel = { showSavePrompt = false },
    )
  }
}

@Composable
private fun SaveOrderDialog(onSave: () -> Unit, onDiscard: () -> Unit, onCancel: () -> Unit) =
  AktualAlertDialog(
    title = Strings.reportsDashboardReorderSaveTitle,
    onDismissRequest = onCancel,
    buttons = {
      TextButton(onClick = onDiscard) {
        Text(Strings.reportsDashboardReorderDiscard, color = colors.warningText)
      }
      TextButton(onClick = onSave) {
        Text(Strings.reportsDashboardReorderSave, color = colors.noticeText)
      }
    },
    content = { Text(Strings.reportsDashboardReorderSaveMessage) },
  )

@Composable
private fun rememberReorderState(
  listState: LazyListState,
  items: ImmutableList<DashboardItem>?,
): DashboardReorderState {
  val scope = rememberCoroutineScope()
  val reorder =
    rememberSaveable(listState, saver = DashboardReorderState.saver(listState, scope)) {
      DashboardReorderState(listState, scope)
    }
  val ids = remember(items) { items.orEmpty().map { it.id } }
  SideEffect { reorder.sync(ids) }
  return reorder
}

// Not SaveableStateProvider: its ReusableContent recycles the top bar's dropdown popup between
// pages and leaks it
@Composable
private fun rememberPageListState(page: DashboardPageId?): LazyListState {
  val states = rememberSaveable(saver = PageListStatesSaver) { mutableMapOf() }
  return remember(page) { states.getOrPut(page?.value.orEmpty()) { LazyListState() } }
}

private const val PINNED_SNACKBAR = "pinned"
private const val DRAGGED_SCALE = 1.03f
private val AutoScrollEdge = 64.dp
private val AutoScrollSpeed = 1000.dp

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
  items: ImmutableList<DashboardItem>?,
  pinnedIds: ImmutableSet<WidgetId>,
  canPin: Boolean,
  customReportNames: ImmutableMap<CustomReportId, String>,
  observer: DashboardItemObserver,
  listState: LazyListState,
  reorder: DashboardReorderState,
  onAction: ActionListener,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  when {
    items == null -> ContentLoading(contentPadding, modifier)
    items.isEmpty() -> ContentEmpty(onAction, modifier)
    else ->
      ContentList(
        items = items,
        pinnedIds = pinnedIds,
        canPin = canPin,
        customReportNames = customReportNames,
        observer = observer,
        listState = listState,
        reorder = reorder,
        onAction = onAction,
        contentPadding = contentPadding,
        modifier = modifier,
      )
  }
}

@Composable
private fun ContentLoading(contentPadding: PaddingValues, modifier: Modifier = Modifier) =
  Column(
    modifier = modifier.padding(horizontal = Dimens.ScreenPadding).padding(contentPadding),
    verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
  ) {
    repeat(times = 5) { ShimmerDashboardItem() }
  }

@Composable
private fun ContentEmpty(onAction: ActionListener, modifier: Modifier = Modifier) =
  Column(
    modifier = modifier.fillMaxSize().padding(40.dp),
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(text = Strings.reportsDashboardEmpty, color = colors.pageText, textAlign = Center)

    VerticalSpacer(20.dp)

    PrimaryTextButton(
      text = Strings.reportsDashboardCreate,
      onClick = { onAction(CreateNewReport) },
    )
  }

@Composable
private fun ContentList(
  items: ImmutableList<DashboardItem>,
  pinnedIds: ImmutableSet<WidgetId>,
  canPin: Boolean,
  customReportNames: ImmutableMap<CustomReportId, String>,
  observer: DashboardItemObserver,
  listState: LazyListState,
  reorder: DashboardReorderState,
  onAction: ActionListener,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  // Only one card can be swiped open at a time
  var openItemId by remember { mutableStateOf<WidgetId?>(null) }
  val haptics = LocalHapticFeedback.current
  val density = LocalDensity.current
  val isEditing = reorder.isEditing
  val byId = remember(items) { items.associateBy { it.id } }
  val ordered = reorder.order.mapNotNull(byId::get)

  SideEffect(isEditing) { openItemId = null }

  LaunchedEffect(reorder, reorder.held) {
    val held = reorder.held ?: return@LaunchedEffect
    with(density) { reorder.autoScroll(held, AutoScrollEdge.toPx(), AutoScrollSpeed.toPx()) }
  }

  LazyColumn(
    modifier = modifier.scrollbar(listState).padding(horizontal = Dimens.ScreenPadding),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
  ) {
    itemsIndexed(ordered, key = { _, item -> item.id.value }) { index, item ->
      val isDragged = reorder.dragged == item.id
      DashboardItem(
        // The handle comes first, so its gesture survives the rest of the chain changing
        modifier =
          Modifier.reorderHandle(reorder, item.id, haptics)
            .then(
              if (isDragged) {
                Modifier.zIndex(1f).graphicsLayer {
                  translationY = reorder.offsetOf(item.id)
                  scaleX = DRAGGED_SCALE
                  scaleY = DRAGGED_SCALE
                }
              } else {
                Modifier.animateItem().wiggle(enabled = isEditing, index = index)
              },
            ),
        item = item,
        isPinned = item.id in pinnedIds,
        canPin = canPin,
        customReportNames = customReportNames,
        observer = observer,
        isOpen = openItemId == item.id,
        onOpenChange = { open ->
          openItemId =
            when {
              open -> item.id
              openItemId == item.id -> null
              else -> openItemId
            }
        },
        onAction = onAction,
        isEditing = isEditing,
      )
    }

    item { BottomSpacing() }
  }
}

@Preview
@Composable
private fun PreviewReportsDashboardScaffold(
  @PreviewParameter(ReportsDashboardScaffoldProvider::class)
  params: ColorParams<ReportsDashboardScaffoldParams>,
) =
  PreviewWithColorParams(params) {
    ReportsDashboardScaffold(
      pages = pages,
      selectedPage = pages.firstOrNull(),
      items = items,
      pinnedIds = persistentSetOf(),
      canPin = true,
      observer = { if (chartData == null) flowOf() else flowOf(chartData) },
      onAction = {},
    )
  }

private data class ReportsDashboardScaffoldParams(
  val pages: ImmutableList<DashboardPage>,
  val items: ImmutableList<DashboardItem>?,
  val chartData: ChartData?,
)

private class ReportsDashboardScaffoldProvider :
  ColorParamsProvider<ReportsDashboardScaffoldParams>(
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
    ReportsDashboardScaffoldParams(pages = persistentListOf(), items = null, chartData = null),
  )
