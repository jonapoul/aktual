package aktual.budget.payees.ui.locations

import aktual.budget.model.PayeeLocationId
import aktual.budget.payees.ui.OpenStreetMapAttribution
import aktual.budget.payees.ui.mapUrl
import aktual.budget.payees.ui.showLocationDeleteFailed
import aktual.budget.payees.ui.showLocationDeleted
import aktual.budget.payees.ui.showLocationRestoreFailed
import aktual.budget.payees.vm.locations.ListPayeeLocationsState
import aktual.budget.payees.vm.locations.ListPayeeLocationsState.Failure
import aktual.budget.payees.vm.locations.ListPayeeLocationsState.Success
import aktual.budget.payees.vm.locations.ListPayeeLocationsViewModel
import aktual.budget.payees.vm.locations.PayeeLocationItem
import aktual.budget.payees.vm.locations.PayeeLocationsEvent.Deleted
import aktual.core.icons.AktualIcons
import aktual.core.icons.Globe
import aktual.core.icons.Refresh
import aktual.core.icons.SearchAlternate
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.SearchPayeeLocationsNavigator
import aktual.core.ui.AktualIconButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.LocalSnackbars
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.NoticeBanner
import aktual.core.ui.OnAction
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.prefs.components.BooleanPreferenceItem
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
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun ListPayeeLocationsScreen(
  back: BackNavigator,
  toSearch: SearchPayeeLocationsNavigator,
  modifier: Modifier = Modifier,
  viewModel: ListPayeeLocationsViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val lookUpAddresses by viewModel.lookUpAddresses.collectAsStateWithLifecycle()
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

  ListPayeeLocationsScaffold(
    modifier = modifier,
    state = state,
    lookUpAddresses = lookUpAddresses,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        Retry -> viewModel.retry()
        OpenSearch -> toSearch()
        is OpenLocation -> uriHandler.openUri(action.coordinates.mapUrl())
        is DeleteLocation -> viewModel.delete(action.id)
        is RequestAddress -> viewModel.requestAddress(action.coordinates)
        is SetLookUpAddresses -> viewModel.setLookUpAddresses(action.enabled)
      }
    },
  )
}

@Composable
private fun ListPayeeLocationsScaffold(
  state: ListPayeeLocationsState,
  lookUpAddresses: Boolean,
  onAction: OnAction<ListPayeeLocationsAction>,
  modifier: Modifier = Modifier,
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = { onAction(NavBack) }) },
        title = { Text(text = Strings.payeesLocationsTitle) },
        actions = {
          if (state is Success) {
            AktualIconButton(
              imageVector = AktualIcons.SearchAlternate,
              contentDescription = Strings.payeesLocationsSearch,
              onClick = { onAction(OpenSearch) },
            )
          }
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      Box(
        modifier =
          Modifier.padding(horizontal = Dimens.ScreenPadding)
            .hazedTopBarContent(hazeState, innerPadding),
        contentAlignment = Center,
      ) {
        ListPayeeLocationsContent(
          state = state,
          lookUpAddresses = lookUpAddresses,
          onAction = onAction,
          listState = listState,
          padding = hazedTopBarContentPadding(hazeState, innerPadding),
        )
      }
    }
  }
}

@Composable
private fun ListPayeeLocationsContent(
  state: ListPayeeLocationsState,
  lookUpAddresses: Boolean,
  onAction: OnAction<ListPayeeLocationsAction>,
  listState: LazyListState,
  padding: PaddingValues,
) {
  when (state) {
    Loading ->
      Column(
        modifier = Modifier.fillMaxSize().padding(padding),
        verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
      ) {
        repeat(PayeeLocationsDS.SHIMMER_ITEMS) { ShimmerPayeeLocationRow() }
      }

    is Failure ->
      FailureScreen(
        title = Strings.payeesLocationsFailure,
        reason = state.cause ?: Strings.payeesFailureDefaultMessage,
        action =
          FailureAction(
            text = { Strings.syncRetry },
            icon = AktualIcons.Refresh,
            onClick = { onAction(Retry) },
          ),
      )

    Empty ->
      FailureScreen(
        title = Strings.payeesLocationsEmpty,
        reason = payeeLocationsHeaderText(),
        action = null,
        icon = null,
      )

    is Success ->
      PayeeLocationsList(
        locations = state.locations,
        lookUpAddresses = lookUpAddresses,
        listState = listState,
        contentPadding = padding,
        onAction = onAction,
      )
  }
}

@Composable
private fun PayeeLocationsList(
  locations: ImmutableList<PayeeLocationItem>,
  lookUpAddresses: Boolean,
  listState: LazyListState,
  contentPadding: PaddingValues,
  onAction: OnAction<ListPayeeLocationsAction>,
  modifier: Modifier = Modifier,
) {
  // Only one row may be swiped open at a time - opening another closes the previous one
  var openId by remember { mutableStateOf<PayeeLocationId?>(null) }

  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
  ) {
    item(key = "notice") { NoticeBanner(text = payeeLocationsHeaderText()) }

    item(key = "addresses") {
      BooleanPreferenceItem(
        value = lookUpAddresses,
        onValueChange = { onAction(SetLookUpAddresses(it)) },
        title = Strings.settingsSecurityLookUpAddresses,
        subtitle = Strings.settingsSecurityLookUpAddressesDesc,
        icon = AktualIcons.Globe,
      )
    }

    items(locations, key = { it.id.value }) { location ->
      SwipeablePayeeLocationRow(
        modifier = Modifier.animateItem(),
        location = location,
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
        onRequestAddress = { onAction(RequestAddress(it)) },
      )
    }

    if (lookUpAddresses) {
      item(key = "attribution") { OpenStreetMapAttribution() }
    }

    item(key = "bottom") { BottomSpacing() }
  }
}

@Composable internal expect fun payeeLocationsHeaderText(): String

private class ListPayeeLocationsStateProvider :
  ColorParamsProvider<ListPayeeLocationsState>(
    Success(PayeeLocationsPreview.all),
    Success(PayeeLocationsPreview.withAddresses),
    Empty,
    Loading,
    Failure("Database connection lost"),
  )

@PortraitPreview
@Composable
private fun PreviewListPayeeLocationsScaffold(
  @PreviewParameter(ListPayeeLocationsStateProvider::class)
  params: ColorParams<ListPayeeLocationsState>,
) =
  PreviewWithColorParams(params) {
    ListPayeeLocationsScaffold(
      state = this,
      lookUpAddresses = this == Success(PayeeLocationsPreview.withAddresses),
      onAction = {},
    )
  }
