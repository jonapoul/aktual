package aktual.about.ui.licenses

import aktual.about.data.ArtifactDetail
import aktual.about.vm.LicensesState
import aktual.about.vm.LicensesState.Error
import aktual.about.vm.LicensesState.Loaded
import aktual.about.vm.LicensesState.Loading
import aktual.about.vm.LicensesState.NoneFound
import aktual.about.vm.LicensesViewModel
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.Search
import aktual.core.icons.material.SearchOff
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTypography
import aktual.core.ui.AnimatedLoading
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithThemedParams
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.WavyBackground
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.blurredTopBarContentPadding
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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

  LicensesScaffold(
    state = licensesState,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        Reload -> viewModel.load()
        OpenSearch -> viewModel.openSearch()
        ClearFilter -> viewModel.clearFilter()
        is EditFilterText -> viewModel.setFilterText(action.text)
        is LaunchUrl -> viewModel.openUrl(action.url)
      }
    },
  )
}

@Composable
private fun LicensesScaffold(state: LicensesState, onAction: LicensesActionHandler) {
  val theme = LocalTheme.current
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()
  val loadedState = state as? Loaded
  val isSearchActive = loadedState?.isSearchActive == true

  Scaffold(
    modifier = Modifier.fillMaxSize().imePadding(),
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, isScrolled = listState.canScrollBackward),
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(NavBack) } },
        title = { Title(isSearchActive, loadedState, onAction) },
        actions = {
          BareIconButton(
            imageVector = if (isSearchActive) MaterialIcons.SearchOff else MaterialIcons.Search,
            contentDescription = Strings.licensesToolbarSearch,
            onClick = { onAction(if (isSearchActive) ClearFilter else OpenSearch) },
          )
        },
      )
    },
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
}

@Composable
private fun Title(isSearchActive: Boolean, loadedState: Loaded?, onAction: LicensesActionHandler) {
  AnimatedContent(
    targetState = isSearchActive,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
  ) { searching ->
    if (searching) {
      CompositionLocalProvider(LocalTextStyle provides AktualTypography.bodyLarge) {
        FilterInput(loadedState?.filterText, loadedState?.artifacts?.size, onAction)
      }
    } else {
      Text(text = Strings.licensesToolbarTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
  }
}

@Composable
private fun FilterInput(
  filterText: String?,
  numResults: Int?,
  onAction: LicensesActionHandler,
  modifier: Modifier = Modifier,
) {
  val state = rememberTextFieldState(initialText = filterText.orEmpty())
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  LaunchedEffect(state) {
    snapshotFlow { state.text.toString() }.collect { filter -> onAction(EditFilterText(filter)) }
  }

  AktualTextField(
    modifier = modifier.focusRequester(focusRequester).fillMaxWidth(),
    state = state,
    singleLine = true,
    placeholderText = Strings.licensesSearchPlaceholder,
    supportingText =
      numResults?.let { n -> { Text(text = Plurals.licensesSearchNumResults(n, n)) } },
  )
}

@Composable
private fun LicensesContent(
  state: LicensesState,
  contentPadding: PaddingValues,
  listState: LazyListState,
  onAction: LicensesActionHandler,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) =
  when (state) {
    Loading -> LoadingContent(modifier)
    NoneFound -> NoneFoundContent(theme, modifier)
    is Loaded -> LoadedContent(theme, state, contentPadding, listState, onAction, modifier)
    is Error -> ErrorContent(theme, state.errorMessage, onAction, modifier)
  }

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) { AnimatedLoading() }
}

@Composable
private fun NoneFoundContent(theme: Theme, modifier: Modifier = Modifier) {
  FailureScreen(
    modifier = modifier,
    title = Strings.licensesError,
    reason = Strings.licensesNoneFound,
    background = theme.tableBackground,
    action = null,
  )
}

@Composable
private fun LoadedContent(
  theme: Theme,
  state: Loaded,
  contentPadding: PaddingValues,
  listState: LazyListState,
  onAction: LicensesActionHandler,
  modifier: Modifier = Modifier,
) {
  if (state.artifacts.isEmpty()) {
    FailureScreen(
      modifier = modifier,
      title = Strings.licensesNoResults,
      reason = null,
      icon = null,
      background = theme.tableBackground,
      action =
        FailureAction(
          text = { Strings.licensesFilterClear },
          icon = MaterialIcons.SearchOff,
          onClick = { onAction(ClearFilter) },
        ),
    )
  } else {
    ArtifactList(theme, state.artifacts, contentPadding, listState, onAction, modifier)
  }
}

@Composable
private fun ArtifactList(
  theme: Theme,
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
        theme = theme,
      )
    }

    item { BottomSpacing() }
  }
}

@Composable
private fun ErrorContent(
  theme: Theme,
  errorMessage: String,
  onAction: LicensesActionHandler,
  modifier: Modifier = Modifier,
) {
  FailureScreen(
    modifier = modifier,
    title = Strings.licensesError,
    reason = Strings.licensesFailed(errorMessage),
    background = theme.tableBackground,
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
  @PreviewParameter(LicensesParamsProvider::class) params: ThemedParams<LicensesState>
) {
  PreviewWithThemedParams(params) { LicensesScaffold(state = this, onAction = {}) }
}

private val LOADED_STATE =
  Loaded(
    artifacts =
      List(size = 5) { listOf(AlakazamAndroidCore, ComposeMaterialRipple, FragmentKtx, Slf4jApi) }
        .flatten()
        .toImmutableList(),
    filterText = "",
    isSearchActive = false,
  )

private class LicensesParamsProvider :
  ThemedParameterProvider<LicensesState>(
    Error("Something broke lol! Here's some more shite to show how it looks"),
    NoneFound,
    Loading,
    LOADED_STATE,
    LOADED_STATE.copy(isSearchActive = true),
    LOADED_STATE.copy(
      artifacts = emptyList<ArtifactDetail>().toImmutableList(),
      isSearchActive = true,
    ),
  )
