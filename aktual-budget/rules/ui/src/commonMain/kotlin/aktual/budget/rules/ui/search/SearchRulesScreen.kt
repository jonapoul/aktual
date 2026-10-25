package aktual.budget.rules.ui.search

import aktual.budget.rules.ui.LocalNameFetcher
import aktual.budget.rules.ui.PreviewRule1
import aktual.budget.rules.ui.PreviewRule2
import aktual.budget.rules.ui.list.ListRulesItemRow
import aktual.budget.rules.ui.list.ShimmerListRulesItem
import aktual.budget.rules.vm.list.CheckboxesState.Inactive
import aktual.budget.rules.vm.search.SearchRulesState
import aktual.budget.rules.vm.search.SearchRulesState.Failure
import aktual.budget.rules.vm.search.SearchRulesState.Results
import aktual.budget.rules.vm.search.SearchRulesViewModel
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.SearchOff
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.EditRuleNavigator
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.Dimens
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun SearchRulesScreen(
  back: BackNavigator,
  editRule: EditRuleNavigator,
  viewModel: SearchRulesViewModel = metroViewModel(),
) {
  val query by viewModel.query.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()

  CompositionLocalProvider(LocalNameFetcher provides viewModel.nameFetcher) {
    SearchRulesScaffold(
      query = query,
      state = state,
      onAction = { action ->
        when (action) {
          NavBack -> back()
          Reload -> viewModel.reload()
          is SetQuery -> viewModel.setQuery(action.query)
          is OpenRule -> editRule(action.id)
        }
      },
    )
  }
}

@Composable
private fun SearchRulesScaffold(
  query: String,
  state: SearchRulesState,
  onAction: SearchRulesActionHandler,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  ScrollToTopOnNewQuery(listState, (state as? Results)?.query)

  SearchScaffold(
    modifier = modifier,
    initialQuery = query,
    placeholder = Strings.rulesSearchPlaceholder,
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
        NoQuery -> SearchMessage(text = Strings.rulesSearchPrompt)

        Loading -> ShimmerResults()

        NoResults ->
          SearchMessage(
            icon = MaterialIcons.SearchOff,
            text = Strings.rulesSearchNoResults(query.trim()),
          )

        is Failure ->
          FailureScreen(
            title = Strings.rulesFailurePrefix,
            reason = animatedState.cause ?: Strings.rulesFailureDefaultMessage,
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
  onAction: SearchRulesActionHandler,
  modifier: Modifier = Modifier,
) =
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = ListPadding,
    verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
  ) {
    items(results.rules, key = { it.id.value }) { rule ->
      ListRulesItemRow(
        modifier = Modifier.animateItem(),
        rule = rule,
        query = results.query,
        onClick = { onAction(OpenRule(rule.id)) },
      )
    }

    item(key = "bottom") { BottomSpacing() }
  }

@Composable
private fun ShimmerResults(modifier: Modifier = Modifier) =
  Column(
    modifier = modifier.fillMaxSize().padding(ListPadding),
    verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
  ) {
    repeat(times = 10) { ShimmerListRulesItem(checkboxes = Inactive) }
  }

private val ListPadding = PaddingValues(horizontal = 8.dp)

@PortraitPreview
@Composable
private fun PreviewSearchRulesScaffold(
  @PreviewParameter(SearchRulesScaffoldProvider::class) params: ColoredParams<SearchRulesParams>,
) =
  PreviewWithColoredParams(params) {
    SearchRulesScaffold(query = query, state = state, onAction = {})
  }

private data class SearchRulesParams(val query: String, val state: SearchRulesState)

private class SearchRulesScaffoldProvider :
  ColoredParameterProvider<SearchRulesParams>(
    SearchRulesParams(query = "", state = NoQuery),
    SearchRulesParams(query = "dummy1", state = Loading),
    SearchRulesParams(query = "xyz", state = NoResults),
    SearchRulesParams(query = "dummy2", state = Failure("Database connection lost")),
    SearchRulesParams(
      query = "dummy3",
      state = Results(query = "dummy3", rules = persistentListOf(PreviewRule1, PreviewRule2)),
    ),
  )
