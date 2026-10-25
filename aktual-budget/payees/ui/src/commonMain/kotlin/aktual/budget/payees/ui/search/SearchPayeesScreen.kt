package aktual.budget.payees.ui.search

import aktual.budget.payees.ui.list.ListPayeesDS
import aktual.budget.payees.ui.list.PayeeItem
import aktual.budget.payees.ui.list.PayeesPreview
import aktual.budget.payees.ui.list.ShimmerPayeeItem
import aktual.budget.payees.ui.openRules
import aktual.budget.payees.vm.search.SearchPayeesState
import aktual.budget.payees.vm.search.SearchPayeesState.Failure
import aktual.budget.payees.vm.search.SearchPayeesState.Results
import aktual.budget.payees.vm.search.SearchPayeesViewModel
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.SearchOff
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.EditPayeeNavigator
import aktual.core.nav.EditRuleNavigator
import aktual.core.nav.SearchRulesNavigator
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
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
internal fun SearchPayeesScreen(
  back: BackNavigator,
  toEdit: EditPayeeNavigator,
  editRule: EditRuleNavigator,
  searchRules: SearchRulesNavigator,
  viewModel: SearchPayeesViewModel = metroViewModel(),
) {
  val query by viewModel.query.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()

  SearchPayeesScaffold(
    query = query,
    state = state,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        Retry -> viewModel.retry()
        is SetQuery -> viewModel.setQuery(action.query)
        is OpenPayee -> toEdit(action.id)
        is OpenRules -> openRules(action.payee, editRule, searchRules)
      }
    },
  )
}

@Composable
private fun SearchPayeesScaffold(
  query: String,
  state: SearchPayeesState,
  onAction: SearchPayeesActionHandler,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  ScrollToTopOnNewQuery(listState, (state as? Results)?.query)

  SearchScaffold(
    modifier = modifier,
    initialQuery = query,
    placeholder = Strings.payeesSearchPlaceholder,
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
        NoQuery -> SearchMessage(text = Strings.payeesSearchPrompt)

        Loading -> ShimmerResults()

        NoResults ->
          SearchMessage(
            icon = MaterialIcons.SearchOff,
            text = Strings.payeesSearchNoResults(query.trim()),
          )

        is Failure ->
          FailureScreen(
            title = Strings.payeesFailurePrefix,
            reason = animatedState.cause ?: Strings.payeesFailureDefaultMessage,
            action =
              FailureAction(
                text = { Strings.syncRetry },
                icon = MaterialIcons.Refresh,
                onClick = { onAction(Retry) },
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
  onAction: SearchPayeesActionHandler,
  modifier: Modifier = Modifier,
) =
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = ListPayeesDS.listPadding,
    verticalArrangement = Arrangement.spacedBy(ListPayeesDS.listItemSpacing),
  ) {
    items(results.payees, key = { it.id.value }) { payee ->
      PayeeItem(
        modifier = Modifier.animateItem(),
        payee = payee,
        query = results.query,
        onClick = { onAction(OpenPayee(payee.id)) },
        onClickRules = { onAction(OpenRules(payee)) },
      )
    }

    item(key = "bottom") { BottomSpacing() }
  }

@Composable
private fun ShimmerResults(modifier: Modifier = Modifier) =
  Column(
    modifier = modifier.fillMaxSize().padding(ListPayeesDS.listPadding),
    verticalArrangement = Arrangement.spacedBy(ListPayeesDS.listItemSpacing),
  ) {
    repeat(ListPayeesDS.SHIMMER_ITEMS) { ShimmerPayeeItem() }
  }

@PortraitPreview
@Composable
private fun PreviewSearchPayeesScaffold(
  @PreviewParameter(SearchPayeesScaffoldProvider::class) params: ColoredParams<SearchPayeesParams>,
) =
  PreviewWithColoredParams(params) {
    SearchPayeesScaffold(query = query, state = state, onAction = {})
  }

private data class SearchPayeesParams(val query: String, val state: SearchPayeesState)

private class SearchPayeesScaffoldProvider :
  ColoredParameterProvider<SearchPayeesParams>(
    SearchPayeesParams(query = "", state = NoQuery),
    SearchPayeesParams(query = "a", state = Loading),
    SearchPayeesParams(query = "xyz", state = NoResults),
    SearchPayeesParams(query = "a", state = Failure("Database connection lost")),
    SearchPayeesParams(
      query = "a",
      state =
        Results(
          query = "a",
          payees = persistentListOf(PayeesPreview.argos, PayeesPreview.transfer),
        ),
    ),
  )
