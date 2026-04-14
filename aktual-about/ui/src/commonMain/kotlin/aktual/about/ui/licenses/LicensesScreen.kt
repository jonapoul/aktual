package aktual.about.ui.licenses

import aktual.about.data.ArtifactDetail
import aktual.about.vm.LicensesState
import aktual.about.vm.LicensesState.Error
import aktual.about.vm.LicensesState.Loaded
import aktual.about.vm.LicensesState.Loading
import aktual.about.vm.LicensesState.NoneFound
import aktual.about.vm.LicensesViewModel
import aktual.about.vm.SearchBarState
import aktual.about.vm.SearchBarState.Gone
import aktual.about.vm.SearchBarState.Visible
import aktual.app.nav.BackNavigator
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.Search
import aktual.core.icons.material.SearchOff
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.AnimatedLoading
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.Dimens
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithThemedParams
import aktual.core.ui.TextField
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.WavyBackground
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.blurredTopBarContentPadding
import aktual.core.ui.keyboardFocusRequester
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.textField
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun LicensesScreen(back: BackNavigator, viewModel: LicensesViewModel = metroViewModel()) {
  val licensesState by viewModel.licensesState.collectAsStateWithLifecycle()
  val searchBarState by viewModel.searchBarState.collectAsStateWithLifecycle()

  LicensesScaffold(
    state = licensesState,
    searchBarState = searchBarState,
    onAction = { action ->
      when (action) {
        LicensesAction.NavBack -> back()
        LicensesAction.Reload -> viewModel.load()
        LicensesAction.ToggleSearchBar -> viewModel.toggleSearchBar()
        is LicensesAction.EditSearchText -> viewModel.setSearchText(action.text)
        is LicensesAction.LaunchUrl -> viewModel.openUrl(action.url)
      }
    },
  )
}

@Composable
private fun LicensesScaffold(
  state: LicensesState,
  searchBarState: SearchBarState,
  onAction: (LicensesAction) -> Unit,
) {
  val theme = LocalTheme.current
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()
  val sheetState = rememberModalBottomSheetState()

  Scaffold(
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, isScrolled = listState.canScrollBackward),
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(LicensesAction.NavBack) } },
        title = {
          Text(text = Strings.licensesToolbarTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        actions = { SearchButton(onAction, searchBarState) },
      )
    }
  ) { innerPadding ->
    Box {
      WavyBackground()
      LicensesContent(
        modifier = Modifier.blurredTopBarContent(blurState, innerPadding),
        state = state,
        contentPadding = blurredTopBarContentPadding(blurState, innerPadding),
        listState = listState,
        theme = theme,
        onAction = onAction,
      )
    }
  }

  if (searchBarState is Visible) {
    SearchInputBottomSheet(
      searchState = searchBarState,
      licensesState = state,
      sheetState = sheetState,
      onAction = onAction,
    )
  }
}

@Composable
private fun SearchButton(onAction: (LicensesAction) -> Unit, state: SearchBarState) {
  IconButton(onClick = { onAction(LicensesAction.ToggleSearchBar) }) {
    Icon(
      imageVector =
        when (state) {
          Gone -> MaterialIcons.Search
          is Visible -> MaterialIcons.SearchOff
        },
      contentDescription = Strings.licensesToolbarSearch,
    )
  }
}

@Composable
private fun SearchInputBottomSheet(
  searchState: Visible,
  licensesState: LicensesState,
  onAction: (LicensesAction) -> Unit,
  sheetState: SheetState,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val keyboard = LocalSoftwareKeyboardController.current
  val isVisible = sheetState.isVisible
  LaunchedEffect(isVisible) { if (isVisible) keyboard?.show() else keyboard?.hide() }

  ModalBottomSheet(
    modifier = modifier,
    onDismissRequest = { onAction(LicensesAction.ToggleSearchBar) },
    sheetState = sheetState,
    containerColor = theme.modalBackground,
    contentColor = theme.pageText,
  ) {
    Column(
      modifier = modifier.padding(Dimens.Huge).fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      TextField(
        modifier = Modifier.fillMaxWidth().focusRequester(keyboardFocusRequester(keyboard)),
        value = searchState.text,
        onValueChange = { query -> onAction(LicensesAction.EditSearchText(query)) },
        placeholderText = Strings.licensesSearchPlaceholder,
        leadingIcon = { Icon(imageVector = MaterialIcons.Search, contentDescription = null) },
        clearable = true,
        theme = theme,
        colors = theme.textField(),
      )

      val numResults = remember(licensesState) { (licensesState as? Loaded)?.artifacts?.size ?: 0 }
      Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.End,
        text = Plurals.licensesSearchNumResults(numResults, numResults),
        style = AktualTypography.labelMedium,
      )
    }
  }
}

@Composable
private fun LicensesContent(
  state: LicensesState,
  contentPadding: PaddingValues,
  listState: LazyListState,
  onAction: (LicensesAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) =
  when (state) {
    Loading -> LoadingContent(modifier)
    NoneFound -> NoneFoundContent(theme, modifier)
    is Loaded ->
      LoadedContent(theme, state.artifacts, contentPadding, listState, onAction, modifier)
    is Error -> ErrorContent(theme, state.errorMessage, onAction, modifier)
  }

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) { AnimatedLoading() }
}

@Composable
private fun NoneFoundContent(theme: Theme, modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    FailureScreen(
      modifier = Modifier.padding(Dimens.Huge).background(theme.tableBackground, CardShape),
      title = Strings.licensesError,
      reason = Strings.licensesNoneFound,
      action = null,
    )
  }
}

@Composable
private fun LoadedContent(
  theme: Theme,
  artifacts: ImmutableList<ArtifactDetail>,
  contentPadding: PaddingValues,
  listState: LazyListState,
  onAction: (LicensesAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(horizontal = 4.dp).scrollbar(listState),
    contentPadding = contentPadding,
    state = listState,
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    items(artifacts) { artifact ->
      ArtifactItem(
        artifact = artifact,
        onLaunchUrl = { onAction(LicensesAction.LaunchUrl(it)) },
        theme = theme,
      )
    }

    item {
      BottomStatusBarSpacing()
      BottomNavBarSpacing()
    }
  }
}

@Composable
private fun ErrorContent(
  theme: Theme,
  errorMessage: String,
  onAction: (LicensesAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    FailureScreen(
      modifier = Modifier.padding(Dimens.Huge).background(theme.tableBackground, CardShape),
      title = Strings.licensesError,
      reason = Strings.licensesFailed(errorMessage),
      action =
        FailureAction(
          text = { Strings.licensesFailedRetry },
          onClick = { onAction(LicensesAction.Reload) },
          icon = MaterialIcons.Refresh,
        ),
    )
  }
}

@PortraitPreview
@Composable
private fun PreviewLicenses(
  @PreviewParameter(LicensesParamsProvider::class) params: ThemedParams<LicensesParams>
) {
  PreviewWithThemedParams(params) {
    LicensesScaffold(state = state, searchBarState = searchState, onAction = {})
  }
}

private data class LicensesParams(val state: LicensesState, val searchState: SearchBarState = Gone)

private val LOADED_STATE =
  Loaded(
    artifacts =
      List(size = 5) { listOf(AlakazamAndroidCore, ComposeMaterialRipple, FragmentKtx, Slf4jApi) }
        .flatten()
        .toImmutableList()
  )

private class LicensesParamsProvider :
  ThemedParameterProvider<LicensesParams>(
    LicensesParams(Error("Something broke lol! Here's some more shite to show how it looks")),
    LicensesParams(NoneFound),
    LicensesParams(Loading),
    LicensesParams(LOADED_STATE, searchState = Gone),
    LicensesParams(LOADED_STATE, searchState = Visible("My wicked search query")),
  )
