package aktual.about.ui.licenses

import aktual.about.data.ArtifactDetail
import aktual.about.vm.LicenseSorting
import aktual.about.vm.LicensesState
import aktual.about.vm.LicensesViewModel
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.Search
import aktual.core.icons.material.Sort
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.SearchLicensesNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AnimatedLoading
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.WavyBackground
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun LicensesScreen(
  back: BackNavigator,
  toSearch: SearchLicensesNavigator,
  viewModel: LicensesViewModel = metroViewModel(),
) {
  val licensesState by viewModel.licensesState.collectAsStateWithLifecycle()
  val sorting by viewModel.sorting.collectAsStateWithLifecycle()
  var showSortSheet by remember { mutableStateOf(false) }

  LicensesScaffold(
    state = licensesState,
    sorting = sorting,
    showSortSheet = showSortSheet,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        Reload -> viewModel.load()
        OpenSearch -> toSearch()
        ShowSortSheet -> showSortSheet = true
        DismissSortSheet -> showSortSheet = false
        is SetSorting -> viewModel.setSorting(action.sorting)
        is LaunchUrl -> viewModel.openUrl(action.url)
      }
    },
  )
}

@Composable
private fun LicensesScaffold(
  state: LicensesState,
  sorting: LicenseSorting,
  showSortSheet: Boolean,
  onAction: LicensesActionHandler,
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()
  val sheetState = rememberBottomSheetState(initialValue = Hidden)

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(NavBack) } },
        title = { Text(text = Strings.licensesToolbarTitle, maxLines = 1, overflow = Ellipsis) },
        actions = {
          if (state is Loaded) {
            BareIconButton(
              imageVector = MaterialIcons.Search,
              contentDescription = Strings.licensesToolbarSearch,
              onClick = { onAction(OpenSearch) },
            )
            BareIconButton(
              imageVector = MaterialIcons.Sort,
              contentDescription = Strings.licensesToolbarSort,
              onClick = { onAction(ShowSortSheet) },
            )
          }
        },
      )
    },
  ) { innerPadding ->
    Box {
      WavyBackground()
      LicensesContent(
        modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
        state = state,
        contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
        listState = listState,
        onAction = onAction,
      )
    }
  }

  if (state is Loaded && showSortSheet) {
    LicenseSortingBottomSheet(sorting, onAction, sheetState)
  }
}

@Composable
private fun LicensesContent(
  state: LicensesState,
  contentPadding: PaddingValues,
  listState: LazyListState,
  onAction: LicensesActionHandler,
  modifier: Modifier = Modifier,
) =
  when (state) {
    Loading -> LoadingContent(modifier)
    NoneFound -> NoneFoundContent(modifier)
    is Loaded -> ArtifactList(state.artifacts, contentPadding, listState, onAction, modifier)
    is LicensesState.Error -> ErrorContent(state.errorMessage, onAction, modifier)
  }

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Center) { AnimatedLoading() }
}

@Composable
private fun NoneFoundContent(modifier: Modifier = Modifier) {
  FailureScreen(
    modifier = modifier,
    title = Strings.licensesError,
    reason = Strings.licensesNoneFound,
    background = colors.tableBackground,
    action = null,
  )
}

@Composable
private fun ArtifactList(
  artifacts: ImmutableList<ArtifactDetail>,
  contentPadding: PaddingValues,
  listState: LazyListState,
  onAction: LicensesActionHandler,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(horizontal = 4.dp).scrollbar(listState),
    contentPadding = contentPadding,
    state = listState,
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    items(artifacts, key = { it.id }) { artifact ->
      ArtifactItem(
        modifier = Modifier.animateItem(),
        artifact = artifact,
        onLaunchUrl = { onAction(LaunchUrl(it)) },
      )
    }

    item { BottomSpacing() }
  }
}

@Composable
private fun ErrorContent(
  errorMessage: String,
  onAction: LicensesActionHandler,
  modifier: Modifier = Modifier,
) {
  FailureScreen(
    modifier = modifier,
    title = Strings.licensesError,
    reason = Strings.licensesFailed(errorMessage),
    background = colors.tableBackground,
    action =
      FailureAction(
        text = { Strings.licensesFailedRetry },
        onClick = { onAction(Reload) },
        icon = MaterialIcons.Refresh,
      ),
  )
}

@PortraitPreview
@Composable
private fun PreviewLicenses(
  @PreviewParameter(LicensesParamsProvider::class) params: ColoredParams<LicensesState>,
) {
  PreviewWithColoredParams(params) {
    LicensesScaffold(state = this, sorting = ByArtifact, showSortSheet = false, onAction = {})
  }
}

private val LOADED_STATE =
  LicensesState.Loaded(
    List(size = 5) { listOf(AlakazamAndroidCore, ComposeMaterialRipple, FragmentKtx, Slf4jApi) }
      .flatten()
      .toImmutableList(),
  )

private class LicensesParamsProvider :
  ColoredParameterProvider<LicensesState>(
    LicensesState.Error("Something broke lol! Here's some more shite to show how it looks"),
    NoneFound,
    Loading,
    LOADED_STATE,
  )
