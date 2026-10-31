package aktual.budget.schedules.ui.list

import aktual.budget.model.ScheduleId
import aktual.budget.model.ScheduleSort
import aktual.budget.schedules.domain.Schedule
import aktual.budget.schedules.ui.list.ListSchedulesPreview.scheduleA
import aktual.budget.schedules.ui.list.ListSchedulesPreview.scheduleB
import aktual.budget.schedules.vm.list.Empty
import aktual.budget.schedules.vm.list.Failure
import aktual.budget.schedules.vm.list.ListSchedulesEvent
import aktual.budget.schedules.vm.list.ListSchedulesEvent.CompleteFailed
import aktual.budget.schedules.vm.list.ListSchedulesEvent.Completed
import aktual.budget.schedules.vm.list.ListSchedulesEvent.DeleteFailed
import aktual.budget.schedules.vm.list.ListSchedulesEvent.Deleted
import aktual.budget.schedules.vm.list.ListSchedulesEvent.PostFailed
import aktual.budget.schedules.vm.list.ListSchedulesEvent.Posted
import aktual.budget.schedules.vm.list.ListSchedulesEvent.RestartFailed
import aktual.budget.schedules.vm.list.ListSchedulesEvent.RestoreFailed
import aktual.budget.schedules.vm.list.ListSchedulesEvent.UnpostFailed
import aktual.budget.schedules.vm.list.ListSchedulesState
import aktual.budget.schedules.vm.list.ListSchedulesViewModel
import aktual.budget.schedules.vm.list.Loading
import aktual.budget.schedules.vm.list.Success
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.Cog
import aktual.core.icons.DotsHorizontalTriple
import aktual.core.icons.Refresh
import aktual.core.icons.SearchAlternate
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Sort
import aktual.core.l10n.Strings
import aktual.core.nav.EditScheduleNavigator
import aktual.core.nav.SearchSchedulesNavigator
import aktual.core.theme.Colors
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParameters
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.LocalSnackbars
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.Snackbars
import aktual.core.ui.SortDialog
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ListSchedulesScreen(
  editSchedule: EditScheduleNavigator,
  toSearch: SearchSchedulesNavigator,
  modifier: Modifier = Modifier,
  viewModel: ListSchedulesViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val showCompleted by viewModel.showCompleted.collectAsStateWithLifecycle()
  val today by viewModel.today.collectAsStateWithLifecycle()
  val snackbars = LocalSnackbars.current
  var posting by remember { mutableStateOf<Schedule?>(null) }

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event -> snackbars.showEvent(event, viewModel) }
  }

  posting?.let { schedule ->
    PostScheduleSheet(
      schedule = schedule,
      today = today,
      onPost = { date -> viewModel.post(schedule, date) },
      onDismiss = { posting = null },
    )
  }

  ListSchedulesScaffold(
    modifier = modifier,
    state = state,
    showCompleted = showCompleted,
    onAction = { action ->
      when (action) {
        Retry -> viewModel.retry()
        CreateNew -> editSchedule()
        is Open -> editSchedule(action.id)
        OpenSearch -> toSearch()
        is Delete -> viewModel.delete(action.schedule)
        is Post -> posting = action.schedule
        is Complete -> viewModel.complete(action.schedule)
        is Restart -> viewModel.restart(action.schedule)
        is SetSort -> viewModel.setSort(action.sort)
        is SetShowCompleted -> viewModel.setShowCompleted(action.show)
      }
    },
  )
}

private fun Snackbars.showEvent(event: ListSchedulesEvent, viewModel: ListSchedulesViewModel) =
  when (event) {
    is Deleted -> show { showDeleted(event, onUndo = viewModel::undoDelete) }
    is DeleteFailed -> show { showDeleteFailed(event) }
    is RestoreFailed -> show { showRestoreFailed(event) }
    is Completed -> show { showCompleted(event, onUndo = viewModel::undoComplete) }
    is CompleteFailed -> show { showCompleteFailed(event) }
    is RestartFailed -> show { showRestartFailed(event) }
    is Posted -> show { showPosted(event, onUndo = viewModel::undoPost) }
    is PostFailed -> show { showPostFailed(event) }
    is UnpostFailed -> show { showUnpostFailed(event) }
  }

@Composable
private fun ListSchedulesScaffold(
  state: ListSchedulesState,
  showCompleted: Boolean,
  onAction: ListSchedulesActionHandler,
  modifier: Modifier = Modifier,
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()
  val successState = state as? Success
  var showSortDialog by remember { mutableStateOf(false) }
  var showSettingsSheet by remember { mutableStateOf(false) }

  if (showSettingsSheet) {
    ListSchedulesSettingsSheet(
      showCompleted = showCompleted,
      onAction = onAction,
      onDismiss = { showSettingsSheet = false },
    )
  }

  if (showSortDialog) {
    val sort = successState?.sort ?: Default
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
              imageVector = AktualIcons.SearchAlternate,
              contentDescription = Strings.listSchedulesSearch,
              onClick = { onAction(OpenSearch) },
            )
          }
          BareIconButton(
            imageVector = AktualIcons.Add,
            contentDescription = Strings.listSchedulesCreate,
            onClick = { onAction(CreateNew) },
          )
          ListSchedulesMenu(
            showSort = successState != null,
            onSort = { showSortDialog = true },
            onSettings = { showSettingsSheet = true },
          )
        },
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()

      Box(
        modifier =
          Modifier.padding(horizontal = Dimens.ScreenPadding)
            .hazedTopBarContent(hazeState, innerPadding),
        contentAlignment = Center,
      ) {
        ListSchedulesContent(
          state = state,
          contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
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
          verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
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
              icon = AktualIcons.Add,
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
              icon = AktualIcons.Refresh,
              onClick = { onAction(Retry) },
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
    verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
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
  showSort: Boolean,
  onSort: () -> Unit,
  onSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    BareIconButton(
      imageVector = AktualIcons.DotsHorizontalTriple,
      contentDescription = Strings.listSchedulesMenu,
      onClick = { expanded = true },
    )

    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      ListSchedulesMenuItems(
        showSort = showSort,
        onSort = {
          expanded = false
          onSort()
        },
        onSettings = {
          expanded = false
          onSettings()
        },
      )
    }
  }
}

@Composable
private fun ListSchedulesMenuItems(showSort: Boolean, onSort: () -> Unit, onSettings: () -> Unit) {
  if (showSort) {
    AktualDropdownMenuItem(
      text = Strings.sort,
      leadingIcon = MaterialIcons.Sort,
      onClick = onSort,
    )
  }
  AktualDropdownMenuItem(
    text = Strings.listSchedulesSettings,
    leadingIcon = AktualIcons.Cog,
    onClick = onSettings,
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
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    Column(Modifier.width(IntrinsicSize.Max).background(colors.menuBackground)) {
      ListSchedulesMenuItems(showSort = true, onSort = {}, onSettings = {})
    }
  }

@Preview
@Composable
private fun PreviewListSchedulesScaffold(
  @PreviewParameter(ListSchedulesProvider::class) params: ColorParams<ListSchedulesState>,
) =
  PreviewWithColorParams(params) {
    ListSchedulesScaffold(state = this, showCompleted = false, onAction = {})
  }

private class ListSchedulesProvider :
  ColorParamsProvider<ListSchedulesState>(
    Success(schedules = persistentListOf(scheduleA, scheduleB), sort = Default),
    Empty,
    Loading,
    Failure("Some problem happened"),
  )
