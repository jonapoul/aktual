package aktual.budget.schedules.ui.list

import aktual.budget.schedules.ui.list.ListSchedulesPreview.scheduleA
import aktual.budget.schedules.ui.list.ListSchedulesPreview.scheduleB
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
import aktual.core.icons.material.Search
import aktual.core.icons.material.SearchOff
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.nav.EditScheduleNavigator
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTypography
import aktual.core.ui.BareIconButton
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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
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
        OpenSearch -> viewModel.openSearch()
        is EditFilterText -> viewModel.setFilterText(action.text)
        ClearFilter -> viewModel.clearFilter()
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
  val successState = state as? Success

  val isSearchActive = successState?.isSearchActive == true

  Scaffold(
    modifier = modifier.fillMaxSize().imePadding(),
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, isScrolled = listState.canScrollBackward),
        colors = theme.transparentTopAppBarColors(),
        title = { Title(isSearchActive, successState, onAction) },
        actions = {
          BareIconButton(
            imageVector = if (isSearchActive) MaterialIcons.SearchOff else MaterialIcons.Search,
            contentDescription = Strings.listSchedulesFilter,
            onClick = { onAction(if (isSearchActive) ClearFilter else OpenSearch) },
          )
        },
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()

      BlurredPullToRefreshBox(
        modifier = Modifier.padding(ListSchedulesDS.listPadding),
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
private fun Title(
  isSearchActive: Boolean,
  successState: Success?,
  onAction: ListSchedulesActionHandler,
) {
  AnimatedContent(
    targetState = isSearchActive,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
  ) { searching ->
    if (searching) {
      CompositionLocalProvider(LocalTextStyle provides AktualTypography.bodyLarge) {
        FilterInput(successState?.filterText, successState?.schedules?.size, onAction)
      }
    } else {
      Text(text = Strings.listSchedulesTitle)
    }
  }
}

@Composable
private fun FilterInput(
  filterText: String?,
  numResults: Int?,
  onAction: ListSchedulesActionHandler,
  modifier: Modifier = Modifier,
) {
  val state = rememberTextFieldState(initialText = filterText.orEmpty())
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  LaunchedEffect(state) {
    snapshotFlow { state.text.toString() }.collect { filter -> onAction(EditFilterText(filter)) }
  }

  AktualTextField(
    modifier = modifier.focusRequester(focusRequester).fillMaxWidth(),
    state = state,
    singleLine = true,
    placeholderText = Strings.listSchedulesFilterPlaceholder,
    supportingText =
      numResults?.let { n -> { Text(text = Plurals.listSchedulesNumResults(n, n)) } },
  )
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
          title = Strings.listSchedulesEmpty,
          reason = null,
          icon = null,
          background = theme.tableBackground,
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
        if (state.schedules.isEmpty()) {
          FailureScreen(
            title = Strings.listSchedulesNoResults,
            reason = null,
            icon = null,
            background = theme.tableBackground,
            action =
              FailureAction(
                text = { Strings.listSchedulesFilterClear },
                icon = MaterialIcons.SearchOff,
                onClick = { onAction(ClearFilter) },
              ),
          )
        } else {
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
    items(schedules, key = { it.id.value }) { schedule ->
      ListSchedulesItem(modifier = Modifier.animateItem(), schedule = schedule, onAction = onAction)
    }
    item { BottomSpacing() }
  }
}

@Preview
@Composable
private fun PreviewListSchedulesScaffold(
  @PreviewParameter(ListSchedulesProvider::class) params: ThemedParams<ListSchedulesState>
) = PreviewWithThemedParams(params) { ListSchedulesScaffold(state = this, onAction = {}) }

private class ListSchedulesProvider :
  ThemedParameterProvider<ListSchedulesState>(
    Success(
      schedules = persistentListOf(scheduleA, scheduleB),
      filterText = "",
      isSearchActive = false,
    ),
    Success(schedules = persistentListOf(scheduleA), filterText = "rent", isSearchActive = true),
    Success(schedules = persistentListOf(), filterText = "rent", isSearchActive = true),
    Empty,
    Loading,
    Failure("Some problem happened"),
  )
