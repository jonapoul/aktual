package aktual.budget.schedules.ui.list

import aktual.app.nav.EditScheduleNavigator
import aktual.budget.schedules.vm.Schedule
import aktual.budget.schedules.vm.list.Empty
import aktual.budget.schedules.vm.list.Failure
import aktual.budget.schedules.vm.list.ListSchedulesState
import aktual.budget.schedules.vm.list.ListSchedulesViewModel
import aktual.budget.schedules.vm.list.Loading
import aktual.budget.schedules.vm.list.Success
import aktual.core.icons.material.Add
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.BlurredPullToRefreshBox
import aktual.core.ui.BottomSpacing
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.PageBackground
import aktual.core.ui.PreviewWithThemedParams
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.blurredTopBar
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
internal fun ListSchedulesScreen(
  editSchedule: EditScheduleNavigator,
  modifier: Modifier = Modifier,
  viewModel: ListSchedulesViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  ListSchedulesScaffold(
    modifier = modifier,
    state = state,
    onAction = { action ->
      when (action) {
        Reload -> viewModel.reload()
        CreateNew -> editSchedule()
        is Open -> editSchedule(action.id)
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
  val theme = LocalTheme.current
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, isScrolled = listState.canScrollBackward),
        colors = theme.transparentTopAppBarColors(),
        title = { Text(Strings.listSchedulesTitle) },
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()

      BlurredPullToRefreshBox(
        modifier = Modifier.padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
        onRefresh = { onAction(Reload) },
        isRefreshing = state is Loading,
        blurState = blurState,
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
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally,
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
          title = Strings.rulesEmpty,
          reason = null,
          icon = null,
          background = theme.tableBackground,
          action =
            FailureAction(
              text = { Strings.rulesEmptyCreate },
              icon = MaterialIcons.Add,
              onClick = { onAction(CreateNew) },
            ),
        )
      }
      is Failure -> {
        FailureScreen(
          title = Strings.rulesFailurePrefix,
          reason = state.cause ?: Strings.rulesFailureDefaultMessage,
          background = theme.tableBackground,
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
  LazyColumn(
    modifier = modifier.scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(ListSchedulesDS.listItemSpacing),
  ) {
    items(schedules) { schedule -> ListSchedulesItem(schedule, onAction) }
    item { BottomSpacing() }
  }
}

@Preview
@Composable
private fun PreviewListSchedulesScaffold(
  @PreviewParameter(BottomBarProvider::class) params: ThemedParams<ListSchedulesParams>
) = PreviewWithThemedParams(params) { ListSchedulesScaffold(state = state, onAction = {}) }

private data class ListSchedulesParams(val state: ListSchedulesState)

private class BottomBarProvider :
  ThemedParameterProvider<ListSchedulesParams>(
    ListSchedulesParams(
      Success(persistentListOf(ListSchedulesPreview.scheduleA, ListSchedulesPreview.scheduleB))
    ),
    ListSchedulesParams(Empty),
    ListSchedulesParams(Loading),
    ListSchedulesParams(Failure("Some problem happened")),
  )
