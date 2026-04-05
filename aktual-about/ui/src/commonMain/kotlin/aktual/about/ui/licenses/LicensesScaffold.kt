package aktual.about.ui.licenses

import aktual.about.data.ArtifactDetail
import aktual.about.vm.LicensesState
import aktual.about.vm.SearchBarState
import aktual.core.icons.material.Error
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Search
import aktual.core.icons.material.SearchOff
import aktual.core.icons.material.Warning
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AnimatedLoading
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.Dimens
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.TextField
import aktual.core.ui.ThemeParameters
import aktual.core.ui.WavyBackground
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.blurredTopBarContentPadding
import aktual.core.ui.keyboardFocusRequester
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.textField
import aktual.core.ui.transparentTopAppBarColors
import alakazam.compose.VerticalSpacer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun LicensesScaffold(
  state: LicensesState,
  searchBarState: SearchBarState,
  onAction: (LicensesAction) -> Unit,
) {
  val theme = LocalTheme.current
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, isScrolled = listState.canScrollBackward),
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(LicensesAction.NavBack) } },
        title = {
          Text(text = Strings.licensesToolbarTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        actions = {
          IconButton(onClick = { onAction(LicensesAction.ToggleSearchBar) }) {
            Icon(
              imageVector =
                when (searchBarState) {
                  SearchBarState.Gone -> MaterialIcons.Search
                  is SearchBarState.Visible -> MaterialIcons.SearchOff
                },
              contentDescription = Strings.licensesToolbarSearch,
            )
          }
        },
      )
    }
  ) { innerPadding ->
    Box {
      WavyBackground()
      Column(modifier = Modifier.blurredTopBarContent(blurState, innerPadding)) {
        LicensesSearchInput(
          modifier =
            Modifier.fillMaxWidth()
              .padding(
                start = 20.dp,
                end = 20.dp,
                top = if (blurState.blurEnabled) innerPadding.calculateTopPadding() else 0.dp,
              )
              .wrapContentHeight(),
          searchState = searchBarState,
          licensesState = state,
          onAction = onAction,
          theme = theme,
        )

        Content(
          state = state,
          contentPadding =
            if (searchBarState is SearchBarState.Visible) {
              PaddingValues.Zero
            } else {
              blurredTopBarContentPadding(blurState, innerPadding)
            },
          listState = listState,
          theme = theme,
          onAction = onAction,
        )
      }
    }
  }
}

@Composable
private fun LicensesSearchInput(
  searchState: SearchBarState,
  licensesState: LicensesState,
  onAction: (LicensesAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val keyboard = LocalSoftwareKeyboardController.current
  val isVisible = searchState is SearchBarState.Visible
  LaunchedEffect(isVisible) { if (isVisible) keyboard?.show() else keyboard?.hide() }

  AnimatedVisibility(
    visible = isVisible,
    enter = slideInVertically() + fadeIn(),
    exit = slideOutVertically() + fadeOut(),
  ) {
    val text = (searchState as? SearchBarState.Visible)?.text.orEmpty()
    val colors =
      theme.textField(text = theme.mobileHeaderTextSubdued, icon = theme.mobileHeaderTextSubdued)

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
      TextField(
        modifier = Modifier.fillMaxWidth().focusRequester(keyboardFocusRequester(keyboard)),
        value = text,
        onValueChange = { query -> onAction(LicensesAction.EditSearchText(query)) },
        placeholderText = Strings.licensesSearchPlaceholder,
        leadingIcon = { Icon(imageVector = MaterialIcons.Search, contentDescription = null) },
        clearable = true,
        theme = theme,
        colors = colors,
      )

      val numResults =
        remember(licensesState) { (licensesState as? LicensesState.Loaded)?.artifacts?.size ?: 0 }
      Text(
        modifier = Modifier.wrapContentWidth().padding(horizontal = Dimens.Large),
        text = Plurals.licensesSearchNumResults(numResults, numResults),
        fontSize = 12.sp,
        color = theme.mobileHeaderTextSubdued,
      )
    }
  }
}

@Composable
private fun Content(
  state: LicensesState,
  contentPadding: PaddingValues,
  listState: LazyListState,
  onAction: (LicensesAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) =
  when (state) {
    LicensesState.Loading -> LoadingContent(modifier)
    LicensesState.NoneFound -> NoneFoundContent(theme, modifier)
    is LicensesState.Loaded ->
      LoadedContent(theme, state.artifacts, contentPadding, listState, onAction, modifier)
    is LicensesState.Error -> ErrorContent(theme, state.errorMessage, onAction, modifier)
  }

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) { AnimatedLoading() }
}

@Composable
private fun NoneFoundContent(theme: Theme, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Icon(
      modifier = Modifier.size(80.dp),
      imageVector = MaterialIcons.Warning,
      contentDescription = null,
      tint = theme.warningText,
    )

    VerticalSpacer(Dimens.Large)

    Text(
      text = Strings.licensesNoneFound,
      fontSize = 20.sp,
      textAlign = TextAlign.Center,
      color = theme.warningText,
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
    modifier = modifier.fillMaxSize().scrollbar(listState).padding(horizontal = Dimens.Large),
    contentPadding = contentPadding,
    state = listState,
  ) {
    items(artifacts) { artifact ->
      ArtifactItem(
        artifact = artifact,
        onLaunchUrl = { onAction(LicensesAction.LaunchUrl(it)) },
        theme = theme,
      )

      VerticalSpacer(5.dp)
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
  Column(
    modifier = modifier.fillMaxSize().padding(32.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Icon(
      modifier = Modifier.size(80.dp),
      imageVector = MaterialIcons.Error,
      contentDescription = null,
      tint = theme.errorText,
    )

    VerticalSpacer(Dimens.Large)

    Text(
      text = Strings.licensesFailed(errorMessage),
      fontSize = 20.sp,
      textAlign = TextAlign.Center,
      color = theme.errorText,
    )

    VerticalSpacer(Dimens.Large)

    PrimaryTextButton(
      text = Strings.licensesFailedRetry,
      onClick = { onAction(LicensesAction.Reload) },
    )
  }
}

@PortraitPreview
@Composable
private fun PreviewNoneFound(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    LicensesScaffold(
      state = LicensesState.NoneFound,
      searchBarState = SearchBarState.Gone,
      onAction = {},
    )
  }

@PortraitPreview
@Composable
private fun PreviewLoading(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    LicensesScaffold(
      state = LicensesState.Loading,
      searchBarState = SearchBarState.Gone,
      onAction = {},
    )
  }

@PortraitPreview
@Composable
private fun PreviewLoaded(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    LicensesScaffold(
      state =
        LicensesState.Loaded(
          artifacts =
            persistentListOf(AlakazamAndroidCore, ComposeMaterialRipple, FragmentKtx, Slf4jApi)
        ),
      searchBarState = SearchBarState.Visible(text = "My wicked search query"),
      onAction = {},
    )
  }

@PortraitPreview
@Composable
private fun PreviewError(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    LicensesScaffold(
      state =
        LicensesState.Error(
          errorMessage = "Something broke lol! Here's some more shite to show how it looks"
        ),
      searchBarState = SearchBarState.Gone,
      onAction = {},
    )
  }
