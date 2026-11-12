package aktual.budget.payees.ui.locations

import aktual.budget.model.PayeeLocationId
import aktual.budget.payees.ui.mapUrl
import aktual.budget.payees.ui.showLocationDeleteFailed
import aktual.budget.payees.ui.showLocationDeleted
import aktual.budget.payees.ui.showLocationRestoreFailed
import aktual.budget.payees.vm.locations.PayeeLocationsEvent.Deleted
import aktual.budget.payees.vm.locations.SearchPayeeLocationsState
import aktual.budget.payees.vm.locations.SearchPayeeLocationsState.Failure
import aktual.budget.payees.vm.locations.SearchPayeeLocationsState.Results
import aktual.budget.payees.vm.locations.SearchPayeeLocationsViewModel
import aktual.core.icons.AktualIcons
import aktual.core.icons.Refresh
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.SearchOff
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.LocalSnackbars
import aktual.core.ui.OnAction
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun SearchPayeeLocationsScreen(
  back: BackNavigator,
  viewModel: SearchPayeeLocationsViewModel = metroViewModel(),
) {
  val query by viewModel.query.collectAsStateWithLifecycle()
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbars = LocalSnackbars.current
  val uriHandler = LocalUriHandler.current

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        is Deleted -> snackbars.show { showLocationDeleted(event.id, viewModel::undoDelete) }
        DeleteFailed -> snackbars.show { showLocationDeleteFailed() }
        RestoreFailed -> snackbars.show { showLocationRestoreFailed() }
      }
    }
  }

  SearchPayeeLocationsScaffold(
    query = query,
    state = state,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        Retry -> viewModel.retry()
        is SetQuery -> viewModel.setQuery(action.query)
        is OpenLocation -> uriHandler.openUri(action.coordinates.mapUrl())
        is DeleteLocation -> viewModel.delete(action.id)
      }
    },
  )
}

@Composable
private fun SearchPayeeLocationsScaffold(
  query: String,
  state: SearchPayeeLocationsState,
  onAction: OnAction<SearchPayeeLocationsAction>,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  ScrollToTopOnNewQuery(listState, (state as? Results)?.query)

  SearchScaffold(
    modifier = modifier,
    initialQuery = query,
    placeholder = Strings.payeesLocationsSearchPlaceholder,
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
            text = Strings.payeesLocationsSearchNoResults(query.trim()),
          )

        is Failure ->
          FailureScreen(
            title = Strings.payeesLocationsFailure,
            reason = animatedState.cause ?: Strings.payeesFailureDefaultMessage,
            action =
              FailureAction(
                text = { Strings.syncRetry },
                icon = AktualIcons.Refresh,
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
  onAction: OnAction<SearchPayeeLocationsAction>,
  modifier: Modifier = Modifier,
) {
  // Only one row may be swiped open at a time - opening another closes the previous one
  var openId by remember { mutableStateOf<PayeeLocationId?>(null) }

  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = PaddingValues(horizontal = Dimens.ScreenPadding),
    verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
  ) {
    items(results.locations, key = { it.id.value }) { location ->
      SwipeablePayeeLocationRow(
        modifier = Modifier.animateItem(),
        location = location,
        query = results.query,
        isOpen = openId == location.id,
        onOpenChange = { open ->
          openId =
            when {
              open -> location.id
              openId == location.id -> null
              else -> openId
            }
        },
        onClick = { onAction(OpenLocation(location.coordinates)) },
        onDelete = { onAction(DeleteLocation(location.id)) },
      )
    }

    item(key = "bottom") { BottomSpacing() }
  }
}

@Composable
private fun ShimmerResults(modifier: Modifier = Modifier) =
  Column(
    modifier = modifier.fillMaxSize().padding(horizontal = Dimens.ScreenPadding),
    verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
  ) {
    repeat(PayeeLocationsDS.SHIMMER_ITEMS) { ShimmerPayeeLocationRow() }
  }

@PortraitPreview
@Composable
private fun PreviewSearchPayeeLocationsScaffold(
  @PreviewParameter(SearchPayeeLocationsProvider::class)
  params: ColorParams<SearchPayeeLocationsParams>,
) =
  PreviewWithColorParams(params) {
    SearchPayeeLocationsScaffold(query = query, state = state, onAction = {})
  }

private data class SearchPayeeLocationsParams(
  val query: String,
  val state: SearchPayeeLocationsState,
)

private class SearchPayeeLocationsProvider :
  ColorParamsProvider<SearchPayeeLocationsParams>(
    SearchPayeeLocationsParams(query = "", state = NoQuery),
    SearchPayeeLocationsParams(query = "t", state = Loading),
    SearchPayeeLocationsParams(query = "xyz", state = NoResults),
    SearchPayeeLocationsParams(query = "t", state = Failure("Database connection lost")),
    SearchPayeeLocationsParams(
      query = "tes",
      state =
        Results(
          query = "tes",
          locations = persistentListOf(PayeeLocationsPreview.tesco, PayeeLocationsPreview.noDate),
        ),
    ),
  )
