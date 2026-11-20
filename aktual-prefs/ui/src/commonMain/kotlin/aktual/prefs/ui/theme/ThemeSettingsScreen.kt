package aktual.prefs.ui.theme

import aktual.core.l10n.Res
import aktual.core.l10n.Strings
import aktual.core.l10n.settings_theme_download_failed_title
import aktual.core.l10n.settings_theme_downloaded
import aktual.core.l10n.settings_theme_refresh_success
import aktual.core.model.ThemeId
import aktual.core.nav.BackNavigator
import aktual.core.nav.InspectThemeNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.theme.DarkColors
import aktual.core.theme.LightColors
import aktual.core.theme.MidnightColors
import aktual.core.ui.AktualSnackbar
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BooleanColorParameters
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParams
import aktual.core.ui.Dimens
import aktual.core.ui.LocalBottomSpacing
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.OnAction
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.bottomNavBarPadding
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.showErrorSnackbar
import aktual.core.ui.showSuccessSnackbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.prefs.components.PreferenceGroup
import aktual.prefs.ui.ProvidePreferenceHighlight
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.theme.CatalogItem
import aktual.prefs.vm.theme.CatalogOptions
import aktual.prefs.vm.theme.CatalogState
import aktual.prefs.vm.theme.LocalTheme
import aktual.prefs.vm.theme.SystemThemePicker
import aktual.prefs.vm.theme.ThemeOption
import aktual.prefs.vm.theme.ThemePreview
import aktual.prefs.vm.theme.ThemeSettingsEvent
import aktual.prefs.vm.theme.ThemeSettingsState
import aktual.prefs.vm.theme.ThemeSettingsViewModel
import aktual.prefs.vm.theme.palette
import aktual.prefs.vm.theme.swatch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.jetbrains.compose.resources.getString

@Composable
fun ThemeSettingsScreen(
  back: BackNavigator,
  toInspectTheme: InspectThemeNavigator,
  highlight: SettingsHighlight?,
  viewModel: ThemeSettingsViewModel = metroViewModel<ThemeSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbar = remember { SnackbarHostState() }
  var picker by remember { mutableStateOf<PickerType?>(null) }
  var showCatalogOptions by remember { mutableStateOf(false) }
  var downloadFailure by remember { mutableStateOf<DownloadFailure?>(null) }

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        CatalogRefreshed ->
          snackbar.showSuccessSnackbar(getString(Res.string.settings_theme_refresh_success))
        is ThemeDownloaded ->
          snackbar.showSuccessSnackbar(getString(Res.string.settings_theme_downloaded, event.name))
        is DownloadFailed ->
          snackbar.showErrorSnackbar(
            getString(Res.string.settings_theme_download_failed_title, event.name),
          )
        is ThemeSettingsEvent.InspectTheme -> toInspectTheme(event.id)
        ThemePicked -> picker = null
      }
    }
  }

  ThemeSettingsScaffold(
    state = state,
    highlight = highlight,
    picker = picker,
    showCatalogOptions = showCatalogOptions,
    downloadFailure = downloadFailure,
    snackbarHostState = snackbar,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        is InspectTheme -> toInspectTheme(action.id)
        is SelectTheme -> viewModel.select(action.id)
        is SetUseSystemDefault -> viewModel.setUseSystemDefault(action.value)
        is PickerAction -> picker = viewModel.onPickerAction(action, picker)
        is CatalogAction ->
          when (action) {
            RefreshCatalog -> viewModel.refreshCatalog()
            is SelectCatalogTheme -> viewModel.selectCatalogTheme(action.summary)
            is InspectCatalogTheme -> viewModel.inspectCatalogTheme(action.summary)
            ShowCatalogOptions -> showCatalogOptions = true
            DismissCatalogOptions -> showCatalogOptions = false
            is SetCatalogOptions -> viewModel.setCatalogOptions(action.options)
            is ShowDownloadFailure -> downloadFailure = action.failure
            DismissDownloadFailure -> downloadFailure = null
          }
      }
    },
  )
}

// Returns the picker to show next
private fun ThemeSettingsViewModel.onPickerAction(
  action: PickerAction,
  picker: PickerType?,
): PickerType? =
  when (action) {
    is ShowPicker -> action.picker
    DismissPicker -> null
    is PickTheme -> {
      when (picker) {
        LightTheme -> pickLightTheme(action.id)
        DarkTheme -> pickDarkTheme(action.id)
        null -> Unit
      }
      picker
    }
  }

@Composable
private fun ThemeSettingsScaffold(
  state: ThemeSettingsState,
  onAction: OnAction<ThemeSettingsAction>,
  highlight: SettingsHighlight? = null,
  picker: PickerType? = null,
  showCatalogOptions: Boolean = false,
  downloadFailure: DownloadFailure? = null,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val listState = rememberLazyListState()
  val hazeState = rememberHazedTopBarState()

  ProvidePreferenceHighlight(highlight, listState) {
    Scaffold(
      topBar = {
        TopAppBar(
          modifier = Modifier.hazedTopBar(hazeState, listState),
          colors = colors.transparentTopAppBarColors(),
          navigationIcon = { NavBackIconButton { onAction(NavBack) } },
          title = { Text(Strings.settingsThemeToolbar) },
        )
      },
      snackbarHost = {
        SnackbarHost(
          hostState = snackbarHostState,
          modifier = Modifier.padding(bottom = LocalBottomSpacing.current + bottomNavBarPadding()),
          snackbar = ::AktualSnackbar,
        )
      },
    ) { innerPadding ->
      Box {
        PageBackground()
        ThemeSettingsContent(
          modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
          contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
          state = state,
          listState = listState,
          onAction = onAction,
        )
      }
    }
  }

  when (picker) {
    LightTheme -> SystemThemeSheet(picker, state.lightTheme, state.customThemeNames, onAction)
    DarkTheme -> SystemThemeSheet(picker, state.darkTheme, state.customThemeNames, onAction)
    null -> Unit
  }

  if (showCatalogOptions) {
    CatalogOptionsSheet(state.catalogOptions, onAction)
  }

  if (downloadFailure != null) {
    DownloadFailedDialog(downloadFailure, onDismiss = { onAction(DismissDownloadFailure) })
  }
}

@Composable
private fun ThemeSettingsContent(
  state: ThemeSettingsState,
  listState: LazyListState,
  contentPadding: PaddingValues,
  onAction: OnAction<ThemeSettingsAction>,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState).padding(Dimens.Large),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item {
      ThemePreviews(
        modifier = Modifier.padding(vertical = 8.dp),
        previews = state.previews,
        customThemeNames = state.customThemeNames,
      )
    }

    item {
      PreferenceGroup(title = Strings.settingsThemeSystemGroup) {
        UseSystemDefaultPreference(preference = state.useSystemDefault, onAction = onAction)
        SystemThemePreference(
          type = LightTheme,
          picker = state.lightTheme,
          customThemeNames = state.customThemeNames,
          onAction = onAction,
        )
        SystemThemePreference(
          type = DarkTheme,
          picker = state.darkTheme,
          customThemeNames = state.customThemeNames,
          onAction = onAction,
        )
      }
    }

    item {
      ThemesPreference(
        localThemes = state.localThemes,
        selectedTheme = state.constantTheme,
        enabled = !state.useSystemDefault.value,
        onAction = onAction,
      )
    }

    item {
      CatalogPreference(
        catalog = state.catalog,
        selectedTheme = state.constantTheme,
        isSelectable = !state.useSystemDefault.value,
        onAction = onAction,
      )
    }

    item { BottomSpacing() }
  }
}

@PortraitPreview
@Composable
private fun PreviewColorsSettings(
  @PreviewParameter(BooleanColorParameters::class) params: ColorParams<Boolean>,
) =
  PreviewWithColorParams(params) {
    ThemeSettingsScaffold(
      state =
        ThemeSettingsState(
          useSystemDefault = BooleanPreference(value = this, enabled = true),
          lightTheme =
            SystemThemePicker(
              value = LightColors.id,
              options = persistentListOf(option(LightColors.id, LightColors.palette())),
              enabled = this,
            ),
          darkTheme =
            SystemThemePicker(
              value = MidnightColors.id,
              options =
                persistentListOf(
                  option(DarkColors.id, DarkColors.palette()),
                  option(MidnightColors.id, MidnightColors.palette()),
                ),
              enabled = this,
            ),
          constantTheme = LightColors.id,
          localThemes =
            persistentListOf(
              LocalTheme(PreviewLocalA, "Light (copy)", LightColors.swatch()),
              LocalTheme(PreviewLocalB, "Midnight with stripes", MidnightColors.swatch()),
            ),
          catalog =
            CatalogState(
              items = persistentListOf(CatalogItem(PreviewSummary, Downloaded)),
              status = Idle,
            ),
          catalogOptions = CatalogOptions(),
          previews =
            if (this) {
              persistentListOf(
                ThemePreview(LightColors.id, Day, LightColors.swatch()),
                ThemePreview(MidnightColors.id, Night, MidnightColors.swatch()),
              )
            } else {
              persistentListOf(
                ThemePreview(LightColors.id, Always, LightColors.swatch()),
              )
            },
          customThemeNames =
            persistentMapOf(
              PreviewLocalA to "Light (copy)",
              PreviewLocalB to "Midnight with stripes",
            ),
        ),
      onAction = {},
    )
  }

private fun option(id: ThemeId, palette: ImmutableList<Color>) =
  ThemeOption(id = id, name = null, palette = palette, isLocal = false, download = null)

private val PreviewLocalA = ThemeId("@local/a")
private val PreviewLocalB = ThemeId("@local/b")
