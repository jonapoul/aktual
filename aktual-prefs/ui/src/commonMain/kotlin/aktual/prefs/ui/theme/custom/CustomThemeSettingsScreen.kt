package aktual.prefs.ui.theme.custom

import aktual.app.nav.BackNavigator
import aktual.app.nav.InspectThemeNavigator
import aktual.core.icons.AktualIcons
import aktual.core.icons.Cloud
import aktual.core.icons.CloudWarning
import aktual.core.icons.material.ArrowRight
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.OfflinePin
import aktual.core.icons.material.Sync
import aktual.core.l10n.Res
import aktual.core.l10n.Strings
import aktual.core.l10n.settings_theme_refresh_failure
import aktual.core.l10n.settings_theme_refresh_success
import aktual.core.theme.CustomThemeSummary
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.Dimens
import aktual.core.ui.FailureScreen
import aktual.core.ui.LocalBottomStatusBarHeight
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithThemedParams
import aktual.core.ui.RowShape
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.blurredTopBarContentPadding
import aktual.core.ui.bottomNavBarPadding
import aktual.core.ui.disabledIf
import aktual.core.ui.radioButton
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.prefs.ui.theme.custom.CustomThemeSettingsAction.ClearCache
import aktual.prefs.ui.theme.custom.CustomThemeSettingsAction.InspectTheme
import aktual.prefs.ui.theme.custom.CustomThemeSettingsAction.NavBack
import aktual.prefs.ui.theme.custom.CustomThemeSettingsAction.RetryFetchCatalog
import aktual.prefs.ui.theme.custom.CustomThemeSettingsAction.SelectTheme
import aktual.prefs.ui.theme.custom.CustomThemeSettingsAction.SetModeFilter
import aktual.prefs.vm.theme.custom.CacheState
import aktual.prefs.vm.theme.custom.CatalogItem
import aktual.prefs.vm.theme.custom.CatalogState
import aktual.prefs.vm.theme.custom.CustomThemeEvent
import aktual.prefs.vm.theme.custom.CustomThemeSettingsViewModel
import aktual.prefs.vm.theme.custom.ThemeFilter
import aktual.prefs.vm.theme.custom.ThemeFilter.All
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.getString

@Composable
fun CustomThemeSettingsScreen(
  back: BackNavigator,
  toInspectTheme: InspectThemeNavigator,
  viewModel: CustomThemeSettingsViewModel = metroViewModel<CustomThemeSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbar = remember { SnackbarHostState() }

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        CustomThemeEvent.CacheRefreshed ->
          snackbar.showSnackbar(getString(Res.string.settings_theme_refresh_success))
        is CustomThemeEvent.FailedFetching ->
          snackbar.showSnackbar(
            getString(Res.string.settings_theme_refresh_failure, event.name, event.reason)
          )
        is CustomThemeEvent.InspectTheme -> toInspectTheme(event.id)
      }
    }
  }

  CustomThemeSettingsScaffold(
    state = state,
    snackbarHostState = snackbar,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        ClearCache -> viewModel.clearCache()
        RetryFetchCatalog -> viewModel.loadCatalog()
        is InspectTheme -> viewModel.fetchAndNavigate(action.summary)
        is SelectTheme -> viewModel.select(action.summary)
        is SetModeFilter -> viewModel.setFilter(action.mode)
      }
    },
  )
}

@Composable
private fun CustomThemeSettingsScaffold(
  state: CatalogState,
  onAction: (CustomThemeSettingsAction) -> Unit,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val theme = LocalTheme.current
  val listState = rememberLazyListState()
  val blurState = rememberBlurredTopBarState()

  Scaffold(
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, isScrolled = listState.canScrollBackward),
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(NavBack) } },
        title = { Text(Strings.settingsThemeCustomTitle) },
        actions = { RefreshButton(state, onAction) },
      )
    },
    snackbarHost = {
      SnackbarHost(
        hostState = snackbarHostState,
        modifier =
          Modifier.padding(
            bottom =
              LocalBottomStatusBarHeight.current + bottomNavBarPadding().calculateBottomPadding()
          ),
      )
    },
  ) { innerPadding ->
    CustomThemeSettingsContent(
      modifier = Modifier.blurredTopBarContent(blurState, innerPadding),
      contentPadding = blurredTopBarContentPadding(blurState, innerPadding),
      state = state,
      listState = listState,
      onAction = onAction,
    )
  }
}

@Composable
private fun RefreshButton(state: CatalogState, onAction: (CustomThemeSettingsAction) -> Unit) {
  BareIconButton(
    imageVector = MaterialIcons.Sync,
    contentDescription = Strings.settingsThemeCustomClearCache,
    enabled = state !is CatalogState.Loading,
    onClick = { onAction(ClearCache) },
  )
}

@Composable
private fun CustomThemeSettingsContent(
  state: CatalogState,
  listState: LazyListState,
  contentPadding: PaddingValues,
  onAction: (CustomThemeSettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    when (state) {
      CatalogState.Loading -> LoadingContent(contentPadding)
      is CatalogState.Failed -> FailedContent(state, onAction)
      is CatalogState.Success ->
        SuccessContent(state.items, state.filter, listState, contentPadding, onAction)
    }
  }
}

@Composable
private fun BoxScope.FailedContent(
  state: CatalogState.Failed,
  onAction: (CustomThemeSettingsAction) -> Unit,
  theme: Theme = LocalTheme.current,
) {
  val cause =
    when (state) {
      CatalogState.Failed.FetchingCatalog -> Strings.settingsThemeRefreshFailure
    }

  FailureScreen(
    modifier = Modifier.align(Alignment.Center).background(theme.tableBackground, CardShape),
    title = Strings.settingsThemeRefreshPrefix,
    reason = cause,
    onClickRetry = { onAction(RetryFetchCatalog) },
    retryText = Strings.settingsThemeRefreshRetry,
  )
}

@Composable
private fun LoadingContent(contentPadding: PaddingValues) {
  LazyColumn(
    modifier = Modifier.padding(Dimens.Large),
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(ITEM_SPACING),
  ) {
    repeat(times = 10) { index -> item(key = index) { LoadingItem() } }
  }
}

@Composable
private fun LoadingItem(modifier: Modifier = Modifier, theme: Theme = LocalTheme.current) {
  val shimmer = rememberShimmer(ShimmerBounds.Window)

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(theme.buttonNormalBackground, RowShape)
        .border(Dp.Hairline, theme.pillBorderDark, RowShape)
        .padding(ITEM_PADDING)
        .shimmer(shimmer),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier =
        Modifier.minimumInteractiveComponentSize()
          .padding(ITEM_PADDING)
          .background(theme.pageText, CardShape)
    )

    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.55f)
            .height(20.dp)
            .background(theme.pageText, CardShape)
      )

      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.35f)
            .height(15.dp)
            .background(theme.pageText, CardShape)
      )

      Row(
        modifier = Modifier.wrapContentWidth(),
        horizontalArrangement = Arrangement.spacedBy(PREVIEW_SPACING),
      ) {
        repeat(times = 6) {
          Box(
            modifier =
              Modifier.weight(PREVIEW_WEIGHT)
                .height(PREVIEW_HEIGHT)
                .background(theme.pageText, CardShape)
          )
        }
      }
    }

    Box(modifier = Modifier.minimumInteractiveComponentSize().background(theme.pageText, CardShape))
  }
}

@Composable
private fun SuccessContent(
  items: ImmutableList<CatalogItem>,
  filter: ThemeFilter,
  listState: LazyListState,
  contentPadding: PaddingValues,
  onAction: (CustomThemeSettingsAction) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.scrollbar(listState).padding(Dimens.Large),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(ITEM_SPACING),
  ) {
    items(items) { item -> CustomThemeItem(item, onAction) }

    item {
      BottomStatusBarSpacing()
      BottomNavBarSpacing()
    }
  }
}

@Composable
internal fun CustomThemeItem(
  item: CatalogItem,
  onAction: (CustomThemeSettingsAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val enabled = item.state !is CacheState.Fetching
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(theme.buttonNormalBackground, RowShape)
        .clickable(enabled) { onAction(SelectTheme(item.summary)) }
        .border(Dp.Hairline, theme.pillBorderDark, RowShape)
        .padding(ITEM_PADDING),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RadioButton(
      modifier = Modifier.padding(8.dp),
      enabled = enabled,
      selected = item.isSelected,
      onClick = null,
      colors = theme.radioButton(),
    )

    Column(
      modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = item.summary.name,
            style = AktualTypography.bodyLarge,
            color = theme.buttonNormalText.disabledIf(!enabled),
            overflow = TextOverflow.Ellipsis,
          )

          Text(
            text = item.summary.repo.toString(),
            style = AktualTypography.labelMedium,
            color = theme.pageTextSubdued.disabledIf(!enabled),
            overflow = TextOverflow.Ellipsis,
          )
        }

        Icon(
          modifier = Modifier.padding(4.dp).size(25.dp),
          imageVector =
            when (item.state) {
              is CacheState.Cached -> MaterialIcons.OfflinePin
              is CacheState.Failed -> AktualIcons.CloudWarning
              CacheState.Fetching -> MaterialIcons.Sync
              CacheState.Remote -> AktualIcons.Cloud
            },
          tint =
            when (item.state) {
              is CacheState.Cached -> theme.pageText
              is CacheState.Failed -> theme.errorText
              CacheState.Fetching -> theme.pageTextSubdued
              CacheState.Remote -> theme.pageText
            },
          contentDescription = null,
        )
      }

      Row(
        modifier = Modifier.wrapContentWidth(),
        horizontalArrangement = Arrangement.spacedBy(PREVIEW_SPACING),
      ) {
        repeat(times = 6) { BoxPreviewColor(summary = item.summary, index = it) }
      }
    }

    NormalIconButton(
      modifier = Modifier.clip(CardShape).fillMaxHeight(),
      imageVector = MaterialIcons.ArrowRight,
      enabled = enabled,
      onClick = { onAction(InspectTheme(item.summary)) },
      contentDescription = Strings.settingsThemePreview(item.summary.name),
    )
  }
}

@Composable
private fun RowScope.BoxPreviewColor(summary: CustomThemeSummary, index: Int) =
  Box(
    modifier =
      Modifier.weight(PREVIEW_WEIGHT)
        .height(PREVIEW_HEIGHT)
        .background(summary.colors[index], CardShape)
  )

private val ITEM_PADDING = PaddingValues(horizontal = 15.dp, vertical = 12.dp)
private val ITEM_SPACING = 4.dp
private val PREVIEW_SPACING = 2.dp
private val PREVIEW_HEIGHT = 20.dp
private const val PREVIEW_WEIGHT = 1f

@Composable
@PortraitPreview
private fun PreviewCustomThemeSettings(
  @PreviewParameter(CatalogStateProvider::class) params: ThemedParams<CatalogState>
) = PreviewWithThemedParams(params) { CustomThemeSettingsScaffold(state = this, onAction = {}) }

private class CatalogStateProvider :
  ThemedParameterProvider<CatalogState>(
    CatalogState.Loading,
    CatalogState.Failed.FetchingCatalog,
    CatalogState.Success(items = persistentListOf(PREVIEW_CATALOG_ITEM), filter = All),
  )
