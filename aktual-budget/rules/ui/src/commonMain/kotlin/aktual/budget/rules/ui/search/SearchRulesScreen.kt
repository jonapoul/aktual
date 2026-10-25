package aktual.budget.rules.ui.search

import aktual.budget.model.PayeeId
import aktual.budget.rules.ui.LocalNameFetcher
import aktual.budget.rules.ui.PreviewRule1
import aktual.budget.rules.ui.PreviewRule2
import aktual.budget.rules.ui.list.ListRulesItemRow
import aktual.budget.rules.ui.list.ShimmerListRulesItem
import aktual.budget.rules.vm.search.SearchRulesState
import aktual.budget.rules.vm.search.SearchRulesState.Failure
import aktual.budget.rules.vm.search.SearchRulesState.Results
import aktual.budget.rules.vm.search.SearchRulesViewModel
import aktual.core.icons.material.Clear
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.SearchOff
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.EditRuleNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun SearchRulesScreen(
  payee: PayeeId?,
  back: BackNavigator,
  editRule: EditRuleNavigator,
  viewModel: SearchRulesViewModel = metroViewModel(payee),
) {
  val query by viewModel.query.collectAsStateWithLifecycle()
  val payeeName by viewModel.payeeName.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()

  CompositionLocalProvider(LocalNameFetcher provides viewModel.nameFetcher) {
    SearchRulesScaffold(
      query = query,
      payeeName = payeeName,
      state = state,
      onAction = { action ->
        when (action) {
          NavBack -> back()
          Reload -> viewModel.reload()
          ClearPayee -> viewModel.clearPayee()
          is SetQuery -> viewModel.setQuery(action.query)
          is OpenRule -> editRule(action.id)
        }
      },
    )
  }
}

@Composable
private fun metroViewModel(payee: PayeeId?) =
  assistedMetroViewModel<SearchRulesViewModel, SearchRulesViewModel.Factory>(
    key = payee.toString(),
    createViewModel = { extras -> create(payee, extras.createSavedStateHandle()) },
  )

@Composable
private fun SearchRulesScaffold(
  query: String,
  payeeName: String?,
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
    Column(modifier = Modifier.padding(innerPadding)) {
      if (payeeName != null) {
        PayeeChip(
          modifier = Modifier.padding(horizontal = Dimens.Large, vertical = Dimens.Medium),
          name = payeeName,
          onRemove = { onAction(ClearPayee) },
        )
      }

      SearchRulesContent(query = query, state = state, listState = listState, onAction = onAction)
    }
  }
}

@Composable
private fun SearchRulesContent(
  query: String,
  state: SearchRulesState,
  listState: LazyListState,
  onAction: SearchRulesActionHandler,
) {
  AnimatedContent(
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
          text =
            if (query.isBlank()) {
              Strings.rulesSearchPayeeNoResults
            } else {
              Strings.rulesSearchNoResults(query.trim())
            },
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

      is Results -> ResultsList(results = animatedState, listState = listState, onAction = onAction)
    }
  }
}

@Composable
private fun PayeeChip(name: String, onRemove: () -> Unit, modifier: Modifier = Modifier) =
  Row(
    modifier =
      modifier
        .heightIn(min = 34.dp)
        .background(colors.pillBackground, CircleShape)
        .padding(start = 12.dp, end = 2.dp),
    verticalAlignment = CenterVertically,
  ) {
    Text(text = name, style = typography.bodyMedium, color = colors.pillText)
    BareIconButton(
      imageVector = MaterialIcons.Clear,
      contentDescription = Strings.rulesSearchPayeeRemove(name),
      size = 16.dp,
      onClick = onRemove,
    )
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
    repeat(times = 10) { ShimmerListRulesItem() }
  }

private val ListPadding = PaddingValues(horizontal = 8.dp)

@PortraitPreview
@Composable
private fun PreviewSearchRulesScaffold(
  @PreviewParameter(SearchRulesScaffoldProvider::class) params: ColoredParams<SearchRulesParams>,
) =
  PreviewWithColoredParams(params) {
    SearchRulesScaffold(query = query, payeeName = payeeName, state = state, onAction = {})
  }

private data class SearchRulesParams(
  val query: String,
  val state: SearchRulesState,
  val payeeName: String? = null,
)

private class SearchRulesScaffoldProvider :
  ColoredParameterProvider<SearchRulesParams>(
    SearchRulesParams(query = "", state = NoQuery),
    SearchRulesParams(
      query = "",
      state = Results(query = "", rules = persistentListOf(PreviewRule1)),
      payeeName = "Tesco",
    ),
    SearchRulesParams(query = "dummy1", state = Loading),
    SearchRulesParams(query = "xyz", state = NoResults),
    SearchRulesParams(query = "dummy2", state = Failure("Database connection lost")),
    SearchRulesParams(
      query = "dummy3",
      state = Results(query = "dummy3", rules = persistentListOf(PreviewRule1, PreviewRule2)),
    ),
  )
