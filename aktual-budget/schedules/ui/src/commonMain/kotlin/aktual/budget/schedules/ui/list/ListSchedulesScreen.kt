package aktual.budget.schedules.ui.list

import aktual.budget.model.ScheduleId
import aktual.budget.model.ScheduleSort
import aktual.budget.schedules.domain.Schedule
import aktual.budget.schedules.ui.list.ListSchedulesPreview.scheduleA
import aktual.budget.schedules.ui.list.ListSchedulesPreview.scheduleB
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
import aktual.core.icons.material.MoreVert
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.Search
import aktual.core.icons.material.Settings
import aktual.core.icons.material.Sort
import aktual.core.l10n.Strings
import aktual.core.nav.EditScheduleNavigator
import aktual.core.nav.ScheduleSettingsNavigator
import aktual.core.nav.SearchSchedulesNavigator
import aktual.core.theme.Colors
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParameters
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.HazedPullToRefreshBox
import aktual.core.ui.LocalSnackbars
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.SortDialog
import aktual.core.ui.hazedTopBar
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ListSchedulesScreen(
  editSchedule: EditScheduleNavigator,
  toSearch: SearchSchedulesNavigator,
  toSettings: ScheduleSettingsNavigator,
  modifier: Modifier = Modifier,
  viewModel: ListSchedulesViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbars = LocalSnackbars.current

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        is Deleted -> snackbars.show { showDeleted(event, onUndo = viewModel::undoDelete) }
        is DeleteFailed -> snackbars.show { showDeleteFailed(event) }
        is RestoreFailed -> snackbars.show { showRestoreFailed(event) }
      }
    }
  }

  // refresh on return (e.g. after creating or editing a schedule) so the list reflects the changes
  @Suppress("ComposeViewModelForwarding")
  LifecycleResumeEffect(viewModel) {
    viewModel.reload(showLoading = false)
    onPauseOrDispose {}
  }

  ListSchedulesScaffold(
    modifier = modifier,
    state = state,
    onAction = { action ->
      when (action) {
        Reload -> viewModel.reload()
        CreateNew -> editSchedule()
        is Open -> editSchedule(action.id)
        OpenSearch -> toSearch()
        is Delete -> viewModel.delete(action.schedule)
        is Post -> snackbars.show { showPostUnsupported() }
        OpenSettings -> toSettings()
        is SetSort -> viewModel.setSort(action.sort)
      }
    },
  )
}

@Composable
private fun ListSchedulesScaffold(
  state: ListSchedulesState,
  onAction: ListSchedulesActionHandler,
  modifier: Modifier = Modifier,
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()
  val successState = state as? Success
  var showSortDialog by remember { mutableStateOf(false) }

  if (showSortDialog) {
    val sort = successState?.sort ?: ScheduleSort.Default
    SortDialog(
      fields = ScheduleSort.Field.Entries,
      field = sort.field,
      direction = sort.direction,
      fieldLabel = { it.label() },
      onConfirm = { field, direction ->
        onAction(SetSort(ScheduleSort(field, direction)))
        showSortDialog = false
      },
      onDismiss = { showSortDialog = false },
    )
  }

  Scaffold(
    modifier = modifier.fillMaxSize().imePadding(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavDrawerIconButton() },
        title = { Text(text = Strings.listSchedulesTitle) },
        actions = {
          if (successState != null) {
            BareIconButton(
              imageVector = MaterialIcons.Search,
              contentDescription = Strings.listSchedulesSearch,
              onClick = { onAction(OpenSearch) },
            )
          }
          BareIconButton(
            imageVector = MaterialIcons.Add,
            contentDescription = Strings.listSchedulesCreate,
            onClick = { onAction(CreateNew) },
          )
          ListSchedulesMenu(
            onAction = onAction,
            showSort = successState != null,
            onSort = { showSortDialog = true },
          )
        },
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

@Composable
private fun ListSchedulesMenu(
  onAction: ListSchedulesActionHandler,
  showSort: Boolean,
  onSort: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    BareIconButton(
      imageVector = MaterialIcons.MoreVert,
      contentDescription = Strings.listSchedulesMenu,
      onClick = { expanded = true },
    )

    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      ListSchedulesMenuItems(
        onAction = { action ->
          expanded = false
          onAction(action)
        },
        showSort = showSort,
        onSort = {
          expanded = false
          onSort()
        },
      )
    }
  }
}

@Composable
private fun ListSchedulesMenuItems(
  onAction: ListSchedulesActionHandler,
  showSort: Boolean,
  onSort: () -> Unit,
) {
  if (showSort) {
    AktualDropdownMenuItem(
      text = Strings.sort,
      leadingIcon = MaterialIcons.Sort,
      onClick = onSort,
    )
  }
  AktualDropdownMenuItem(
    text = Strings.listSchedulesSettings,
    leadingIcon = MaterialIcons.Settings,
    onClick = { onAction(OpenSettings) },
  )
}

@Composable
private fun ScheduleSort.Field.label(): String =
  when (this) {
    NextDate -> Strings.listSchedulesSortNextDate
    Name -> Strings.listSchedulesSortName
    Payee -> Strings.listSchedulesSortPayee
    Account -> Strings.listSchedulesSortAccount
    Amount -> Strings.listSchedulesSortAmount
    Status -> Strings.listSchedulesSortStatus
  }

@Preview
@Composable
private fun PreviewListSchedulesMenuItems(
  @PreviewParameter(ColoredParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    Column(Modifier.width(IntrinsicSize.Max).background(colors.menuBackground)) {
      ListSchedulesMenuItems(onAction = {}, showSort = true, onSort = {})
    }
  }

@Preview
@Composable
private fun PreviewListSchedulesScaffold(
  @PreviewParameter(ListSchedulesProvider::class) params: ColoredParams<ListSchedulesState>,
) =
  PreviewWithColoredParams(params) {
    ListSchedulesScaffold(state = this, onAction = {})
  }

private class ListSchedulesProvider :
  ColoredParameterProvider<ListSchedulesState>(
    Success(schedules = persistentListOf(scheduleA, scheduleB), sort = Default),
    Empty,
    Loading,
    Failure("Some problem happened"),
  )
