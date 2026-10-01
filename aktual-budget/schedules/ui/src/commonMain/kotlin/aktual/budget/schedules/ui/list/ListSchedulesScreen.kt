package aktual.budget.schedules.ui.list

import aktual.budget.model.ScheduleId
import aktual.budget.schedules.ui.list.ListSchedulesPreview.scheduleA
import aktual.budget.schedules.ui.list.ListSchedulesPreview.scheduleB
import aktual.budget.schedules.vm.Schedule
import aktual.budget.schedules.vm.list.Empty
import aktual.budget.schedules.vm.list.Failure
import aktual.budget.schedules.vm.list.ListSchedulesEvent.DeleteFailed
import aktual.budget.schedules.vm.list.ListSchedulesEvent.Deleted
import aktual.budget.schedules.vm.list.ListSchedulesEvent.RestoreFailed
import aktual.budget.schedules.vm.list.ListSchedulesState
import aktual.budget.schedules.vm.list.ListSchedulesViewModel
import aktual.budget.schedules.vm.list.Loading
import aktual.budget.schedules.vm.list.Success
import aktual.core.icons.material.Add
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.Search
import aktual.core.l10n.Strings
import aktual.core.nav.EditScheduleNavigator
import aktual.core.nav.SearchSchedulesNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.HazedPullToRefreshBox
import aktual.core.ui.LocalBottomSpacing
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.bottomNavBarPadding
import aktual.core.ui.hazedTopBar
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@Composable
internal fun ListSchedulesScreen(
  editSchedule: EditScheduleNavigator,
  toSearch: SearchSchedulesNavigator,
  modifier: Modifier = Modifier,
  viewModel: ListSchedulesViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbar = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        is Deleted -> snackbar.showDeleted(event, onUndo = viewModel::undoDelete)
        is DeleteFailed -> snackbar.showDeleteFailed(event)
        is RestoreFailed -> snackbar.showRestoreFailed(event)
      }
    }
  }

  ListSchedulesScaffold(
    modifier = modifier,
    state = state,
    snackbarHostState = snackbar,
    onAction = { action ->
      when (action) {
        Reload -> viewModel.reload()
        CreateNew -> editSchedule()
        is Open -> editSchedule(action.id)
        OpenSearch -> toSearch()
        is Delete -> viewModel.delete(action.schedule)
        is Post -> scope.launch { snackbar.showPostUnsupported() }
      }
    },
  )
}

@Composable
private fun ListSchedulesScaffold(
  state: ListSchedulesState,
  onAction: ListSchedulesActionHandler,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = modifier.fillMaxSize().imePadding(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavDrawerIconButton() },
        title = { Text(text = Strings.listSchedulesTitle) },
        actions = {
          if (state is Success) {
            BareIconButton(
              imageVector = MaterialIcons.Search,
              contentDescription = Strings.listSchedulesSearch,
              onClick = { onAction(OpenSearch) },
            )
          }
        },
      )
    },
    snackbarHost = {
      SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(bottom = LocalBottomSpacing.current + bottomNavBarPadding()),
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()

      HazedPullToRefreshBox(
        modifier = Modifier.padding(ListSchedulesDS.listPadding),
        contentAlignment = Center,
        onRefresh = { onAction(Reload) },
        isRefreshing = state is Loading,
        hazeState = hazeState,
        innerPadding = innerPadding,
      ) { padding ->
        ListSchedulesContent(
          state = state,
          contentPadding = padding,
          onAction = onAction,
          listState = listState,
        )
      }
    }
  }
}

@Composable
private fun ListSchedulesContent(
  state: ListSchedulesState,
  contentPadding: PaddingValues,
  onAction: ListSchedulesActionHandler,
  listState: LazyListState,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = CenterHorizontally,
  ) {
    when (state) {
      Loading -> {
        Column(
          modifier = Modifier.padding(contentPadding),
          verticalArrangement = Arrangement.spacedBy(ListSchedulesDS.listItemSpacing),
        ) {
          repeat(times = 10) { ShimmerListSchedulesItem() }
          BottomSpacing()
        }
      }
      Empty -> {
        FailureScreen(
          title = Strings.listSchedulesEmpty,
          reason = null,
          icon = null,
          background = colors.tableBackground,
          action =
            FailureAction(
              text = { Strings.listSchedulesEmptyCreate },
              icon = MaterialIcons.Add,
              onClick = { onAction(CreateNew) },
            ),
        )
      }
      is Failure -> {
        FailureScreen(
          title = Strings.listSchedulesFailurePrefix,
          reason = state.cause ?: Strings.listSchedulesFailureDefaultMessage,
          background = colors.tableBackground,
          action =
            FailureAction(
              text = { Strings.syncRetry },
              icon = MaterialIcons.Refresh,
              onClick = { onAction(Reload) },
            ),
        )
      }
      is Success -> {
        ContentSuccess(
          schedules = state.schedules,
          listState = listState,
          contentPadding = contentPadding,
          onAction = onAction,
        )
      }
    }
  }
}

@Composable
private fun ContentSuccess(
  schedules: ImmutableList<Schedule>,
  listState: LazyListState,
  contentPadding: PaddingValues,
  onAction: ListSchedulesActionHandler,
  modifier: Modifier = Modifier,
) {
  // only one row may be swiped open at a time - opening another closes the previous one
  var openId by remember { mutableStateOf<ScheduleId?>(null) }

  LazyColumn(
    modifier = modifier.scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(ListSchedulesDS.listItemSpacing),
  ) {
    items(schedules, key = { it.id.value }) { schedule ->
      SwipeableListSchedulesItem(
        modifier = Modifier.animateItem(),
        schedule = schedule,
        isOpen = openId == schedule.id,
        onOpenChange = { open ->
          openId =
            when {
              open -> schedule.id
              openId == schedule.id -> null
              else -> openId
            }
        },
        onAction = onAction,
      )
    }
    item { BottomSpacing() }
  }
}

@Preview
@Composable
private fun PreviewListSchedulesScaffold(
  @PreviewParameter(ListSchedulesProvider::class) params: ColoredParams<ListSchedulesState>
) = PreviewWithColoredParams(params) { ListSchedulesScaffold(state = this, onAction = {}) }

private class ListSchedulesProvider :
  ColoredParameterProvider<ListSchedulesState>(
    Success(schedules = persistentListOf(scheduleA, scheduleB)),
    Empty,
    Loading,
    Failure("Some problem happened"),
  )
