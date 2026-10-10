package aktual.about.ui.licenses.search

import aktual.about.ui.licenses.AlakazamAndroidCore
import aktual.about.ui.licenses.ArtifactItem
import aktual.about.ui.licenses.ComposeMaterialRipple
import aktual.about.vm.SearchLicensesState
import aktual.about.vm.SearchLicensesState.Results
import aktual.about.vm.SearchLicensesViewModel
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.SearchOff
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun SearchLicensesScreen(
  back: BackNavigator,
  viewModel: SearchLicensesViewModel = metroViewModel(),
) {
  val query by viewModel.query.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()

  SearchLicensesScaffold(
    query = query,
    state = state,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        is SetQuery -> viewModel.setQuery(action.query)
        is LaunchUrl -> viewModel.openUrl(action.url)
      }
    },
  )
}

@Composable
private fun SearchLicensesScaffold(
  query: String,
  state: SearchLicensesState,
  onAction: SearchLicensesActionHandler,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  ScrollToTopOnNewQuery(listState = listState, query = (state as? Results)?.query)

  SearchScaffold(
    modifier = modifier,
    initialQuery = query,
    placeholder = Strings.licensesSearchPlaceholder,
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
        NoQuery -> SearchMessage(text = Strings.licensesSearchPrompt)

        NoResults ->
          SearchMessage(
            icon = MaterialIcons.SearchOff,
            text = Strings.licensesSearchNoResults(query.trim()),
          )

        is Results -> ResultsList(animatedState, listState, onAction)
      }
    }
  }
}

@Composable
private fun ResultsList(
  results: Results,
  listState: LazyListState,
  onAction: SearchLicensesActionHandler,
  modifier: Modifier = Modifier,
) =
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(horizontal = 4.dp).scrollbar(listState),
    state = listState,
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    items(results.artifacts, key = { it.id }) { artifact ->
      ArtifactItem(
        modifier = Modifier.animateItem(),
        artifact = artifact,
        onLaunchUrl = { onAction(LaunchUrl(it)) },
        query = results.query,
      )
    }

    item(key = "bottom") { BottomSpacing() }
  }

@PortraitPreview
@Composable
private fun PreviewSearchLicensesScaffold(
  @PreviewParameter(SearchLicensesScaffoldProvider::class)
  params: ColoredParams<SearchLicensesParams>,
) =
  PreviewWithColoredParams(params) {
    SearchLicensesScaffold(query = query, state = state, onAction = {})
  }

private data class SearchLicensesParams(val query: String, val state: SearchLicensesState)

private class SearchLicensesScaffoldProvider :
  ColoredParameterProvider<SearchLicensesParams>(
    SearchLicensesParams(query = "", state = NoQuery),
    SearchLicensesParams(query = "xyz", state = NoResults),
    SearchLicensesParams(
      query = "core",
      state = Results("core", persistentListOf(AlakazamAndroidCore, ComposeMaterialRipple)),
    ),
  )
