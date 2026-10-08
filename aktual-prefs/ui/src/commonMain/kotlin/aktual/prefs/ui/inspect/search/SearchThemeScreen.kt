package aktual.prefs.ui.inspect.search

import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.SearchOff
import aktual.core.l10n.Strings
import aktual.core.model.ThemeId
import aktual.core.nav.BackNavigator
import aktual.core.theme.LightColors
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.Dimens
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.ScrollToTopOnNewQuery
import aktual.core.ui.SearchMessage
import aktual.core.ui.SearchScaffold
import aktual.core.ui.scrollbar
import aktual.prefs.ui.inspect.ThemePropertyRow
import aktual.prefs.vm.inspect.search.SearchThemeState
import aktual.prefs.vm.inspect.search.SearchThemeState.Results
import aktual.prefs.vm.inspect.search.SearchThemeViewModel
import aktual.prefs.vm.theme.properties
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun SearchThemeScreen(
  back: BackNavigator,
  themeId: ThemeId,
  viewModel: SearchThemeViewModel = metroViewModel(themeId),
) {
  val query by viewModel.query.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()

  SearchThemeScaffold(
    query = query,
    state = state,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        is SetQuery -> viewModel.setQuery(action.query)
      }
    },
  )
}

@Composable
private fun metroViewModel(themeId: ThemeId) =
  assistedMetroViewModel<SearchThemeViewModel, SearchThemeViewModel.Factory>(
    key = themeId.value,
    createViewModel = { create(themeId) },
  )

@Composable
private fun SearchThemeScaffold(
  query: String,
  state: SearchThemeState,
  onAction: SearchThemeActionHandler,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  ScrollToTopOnNewQuery(listState = listState, query = (state as? Results)?.query)

  SearchScaffold(
    modifier = modifier,
    initialQuery = query,
    placeholder = Strings.settingsThemeInspectSearchPlaceholder,
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
        NoQuery -> SearchMessage(text = Strings.settingsThemeInspectSearchPrompt)

        NoResults ->
          SearchMessage(
            icon = MaterialIcons.SearchOff,
            text = Strings.settingsThemeInspectSearchNoResults(query.trim()),
          )

        is Results -> ResultsList(results = animatedState, listState = listState)
      }
    }
  }
}

@Composable
private fun ResultsList(
  results: Results,
  listState: LazyListState,
  modifier: Modifier = Modifier,
) =
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState).padding(Dimens.Large),
    state = listState,
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    items(results.properties, key = { it.name }) { property ->
      ThemePropertyRow(property, modifier = Modifier.animateItem(), query = results.query)
    }

    item(key = "bottom") { BottomSpacing() }
  }

@PortraitPreview
@Composable
private fun PreviewSearchThemeScaffold(
  @PreviewParameter(SearchThemeScaffoldProvider::class) params: ColoredParams<SearchThemeParams>,
) =
  PreviewWithColoredParams(params) {
    SearchThemeScaffold(query = query, state = state, onAction = {})
  }

private data class SearchThemeParams(val query: String, val state: SearchThemeState)

private class SearchThemeScaffoldProvider :
  ColoredParameterProvider<SearchThemeParams>(
    SearchThemeParams(query = "", state = NoQuery),
    SearchThemeParams(query = "xyz", state = NoResults),
    SearchThemeParams(
      query = "page",
      state =
        Results(
          query = "page",
          properties =
            LightColors.properties().filter { "page" in it.name.lowercase() }.toImmutableList(),
        ),
    ),
  )
