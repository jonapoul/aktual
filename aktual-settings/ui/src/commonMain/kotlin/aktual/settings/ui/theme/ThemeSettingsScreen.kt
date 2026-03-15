package aktual.settings.ui.theme

import aktual.core.l10n.Strings
import aktual.core.theme.DarkTheme
import aktual.core.theme.LightTheme
import aktual.core.theme.LocalTheme
import aktual.core.theme.MidnightTheme
import aktual.core.theme.Theme
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.DesktopPreview
import aktual.core.ui.Dimens
import aktual.core.ui.LandscapePreview
import aktual.core.ui.LocalBottomStatusBarHeight
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.TabletPreview
import aktual.core.ui.ThemeParameters
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.settings.vm.BooleanPreference
import aktual.settings.vm.ListPreference
import aktual.settings.vm.theme.CatalogState
import aktual.settings.vm.theme.ThemeSettingsEvent
import aktual.settings.vm.theme.ThemeSettingsState
import aktual.settings.vm.theme.ThemeSettingsViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ThemeSettingsScreen(
  nav: ThemeSettingsNavigator,
  viewModel: ThemeSettingsViewModel = metroViewModel<ThemeSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val catalogState by viewModel.catalogState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  val refreshSuccessMessage = Strings.settingsThemeRefreshSuccess
  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        ThemeSettingsEvent.CacheRefreshed -> snackbarHostState.showSnackbar(refreshSuccessMessage)
      }
    }
  }

  ThemeSettingsScaffold(
    state = state,
    catalogState = catalogState,
    snackbarHostState = snackbarHostState,
    onAction = { action ->
      when (action) {
        ThemeSettingsAction.NavBack -> nav.back()
        is ThemeSettingsAction.InspectTheme -> nav.inspectTheme(action.id)
        is ThemeSettingsAction.SelectTheme -> viewModel.select(action.id)
        ThemeSettingsAction.ClearCache -> viewModel.clearCache()
        ThemeSettingsAction.RetryFetchCatalog -> viewModel.retry()
        is ThemeSettingsAction.SetUseSystemDefault -> viewModel.setUseSystemDefault(action.value)
        is ThemeSettingsAction.SetDarkTheme -> viewModel.setDarkTheme(action.value)
      }
    },
  )
}

@Composable
private fun ThemeSettingsScaffold(
  state: ThemeSettingsState,
  catalogState: CatalogState,
  onAction: (ThemeSettingsAction) -> Unit,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val theme = LocalTheme.current

  Scaffold(
    topBar = {
      TopAppBar(
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(ThemeSettingsAction.NavBack) } },
        title = { Text(Strings.settingsThemeToolbar) },
      )
    },
    snackbarHost = {
      SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(bottom = LocalBottomStatusBarHeight.current),
      )
    },
  ) { innerPadding ->
    ThemeSettingsContent(
      modifier = Modifier.padding(innerPadding),
      state = state,
      catalogState = catalogState,
      onAction = onAction,
    )
  }
}

@Composable
private fun ThemeSettingsContent(
  state: ThemeSettingsState,
  catalogState: CatalogState,
  onAction: (ThemeSettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState).padding(horizontal = Dimens.Large),
    state = listState,
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item { UseSystemDefaultPreference(preference = state.useSystemDefault, onAction = onAction) }

    item { DarkThemePreference(preference = state.darkTheme, onAction = onAction) }

    item {
      BuiltInThemesPreference(
        selectedTheme = state.constantTheme,
        enabled = !state.useSystemDefault.value,
        onAction = onAction,
      )
    }

    item {
      CustomThemesPreference(
        selectedTheme = state.constantTheme,
        enabled = !state.useSystemDefault.value,
        state = catalogState,
        onAction = onAction,
      )
    }

    item {
      BottomStatusBarSpacing()
      BottomNavBarSpacing()
    }
  }
}

@PortraitPreview
@LandscapePreview
@TabletPreview
@DesktopPreview
@Composable
private fun PreviewThemeSettings(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithColorScheme(theme) {
    ThemeSettingsScaffold(
      state =
        ThemeSettingsState(
          useSystemDefault = BooleanPreference(value = false, enabled = true),
          darkTheme =
            ListPreference(
              selected = DarkTheme.id,
              values = persistentListOf(DarkTheme.id, MidnightTheme.id),
              enabled = false,
            ),
          constantTheme = LightTheme.id,
        ),
      catalogState = CatalogState.Success(themes = persistentListOf(PREVIEW_CATALOG_ITEM)),
      onAction = {},
    )
  }
