package aktual.budget.schedules.ui.search

import aktual.budget.schedules.ui.list.ListSchedulesDS
import aktual.budget.schedules.ui.list.ListSchedulesItem
import aktual.budget.schedules.ui.list.ListSchedulesPreview
import aktual.budget.schedules.vm.search.SearchSchedulesState
import aktual.budget.schedules.vm.search.SearchSchedulesState.Failure
import aktual.budget.schedules.vm.search.SearchSchedulesState.Results
import aktual.budget.schedules.vm.search.SearchSchedulesViewModel
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.SearchOff
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.EditScheduleNavigator
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.LoadingScreen
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.ScrollToTopOnNewQuery
import aktual.core.ui.SearchMessage
import aktual.core.ui.SearchScaffold
import aktual.core.ui.scrollbar
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun SearchSchedulesScreen(
  back: BackNavigator,
  editSchedule: EditScheduleNavigator,
  viewModel: SearchSchedulesViewModel = metroViewModel(),
) {
  val query by viewModel.query.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()

  SearchSchedulesScaffold(
    query = query,
    state = state,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        Reload -> viewModel.reload()
        is SetQuery -> viewModel.setQuery(action.query)
        is OpenSchedule -> editSchedule(action.id)
      }
    },
  )
}

@Composable
private fun SearchSchedulesScaffold(
  query: String,
  state: SearchSchedulesState,
  onAction: SearchSchedulesActionHandler,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  ScrollToTopOnNewQuery(listState, (state as? Results)?.query)

  SearchScaffold(
    modifier = modifier,
    initialQuery = query,
    placeholder = Strings.listSchedulesSearchPlaceholder,
    listState = listState,
    onQueryChange = { onAction(SetQuery(it)) },
    onBack = { onAction(NavBack) },
  ) { innerPadding ->
    AnimatedContent(
      modifier = Modifier.padding(innerPadding),
      targetState = state,
      contentKey = { it::class },
      transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { animatedState ->
      when (animatedState) {
        NoQuery -> SearchMessage(text = Strings.listSchedulesSearchPrompt)

        Loading -> LoadingScreen()

        NoResults ->
          SearchMessage(
            icon = MaterialIcons.SearchOff,
            text = Strings.listSchedulesSearchNoResults(query.trim()),
          )

        is Failure ->
          FailureScreen(
            title = Strings.listSchedulesFailurePrefix,
            reason = animatedState.cause ?: Strings.listSchedulesFailureDefaultMessage,
            action =
              FailureAction(
                text = { Strings.syncRetry },
                icon = MaterialIcons.Refresh,
                onClick = { onAction(Reload) },
              ),
          )

        is Results ->
          ResultsList(results = animatedState, listState = listState, onAction = onAction)
      }
    }
  }
}

@Composable
private fun ResultsList(
  results: Results,
  listState: LazyListState,
  onAction: SearchSchedulesActionHandler,
  modifier: Modifier = Modifier,
) =
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = ListSchedulesDS.listPadding,
    verticalArrangement = Arrangement.spacedBy(ListSchedulesDS.listItemSpacing),
  ) {
    items(results.schedules, key = { it.id.value }) { schedule ->
      ListSchedulesItem(
        modifier = Modifier.animateItem(),
        schedule = schedule,
        query = results.query,
        onClick = { onAction(OpenSchedule(schedule.id)) },
      )
    }

    item(key = "bottom") { BottomSpacing() }
  }

@PortraitPreview
@Composable
private fun PreviewSearchSchedulesScaffold(
  @PreviewParameter(SearchSchedulesScaffoldProvider::class)
  params: ColoredParams<SearchSchedulesParams>
) =
  PreviewWithColoredParams(params) {
    SearchSchedulesScaffold(query = query, state = state, onAction = {})
  }

private data class SearchSchedulesParams(val query: String, val state: SearchSchedulesState)

private class SearchSchedulesScaffoldProvider :
  ColoredParameterProvider<SearchSchedulesParams>(
    SearchSchedulesParams(query = "", state = NoQuery),
    SearchSchedulesParams(query = "rent", state = Loading),
    SearchSchedulesParams(query = "xyz", state = NoResults),
    SearchSchedulesParams(query = "rent", state = Failure("Database connection lost")),
    SearchSchedulesParams(
      query = "check",
      state =
        Results(
          query = "check",
          schedules =
            persistentListOf(ListSchedulesPreview.scheduleA, ListSchedulesPreview.scheduleB),
        ),
    ),
  )
