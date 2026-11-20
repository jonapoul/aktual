package aktual.prefs.ui.theme

import aktual.core.l10n.Strings
import aktual.core.model.ThemeId
import aktual.core.nav.BackNavigator
import aktual.core.nav.CustomThemesNavigator
import aktual.core.nav.InspectThemeNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.theme.DarkColors
import aktual.core.theme.LightColors
import aktual.core.theme.MidnightColors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BooleanColorParameters
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParams
import aktual.core.ui.Dimens
import aktual.core.ui.NavBackIconButton
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
import aktual.prefs.components.PreferenceGroup
import aktual.prefs.ui.ProvidePreferenceHighlight
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.theme.LocalTheme
import aktual.prefs.vm.theme.ThemePreview
import aktual.prefs.vm.theme.ThemeSettingsState
import aktual.prefs.vm.theme.ThemeSettingsViewModel
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@Composable
fun ThemeSettingsScreen(
  back: BackNavigator,
  toCustomThemes: CustomThemesNavigator,
  toInspectTheme: InspectThemeNavigator,
  highlight: SettingsHighlight?,
  viewModel: ThemeSettingsViewModel = metroViewModel<ThemeSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  ThemeSettingsScaffold(
    state = state,
    highlight = highlight,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        NavCustomThemes -> toCustomThemes()
        is InspectTheme -> toInspectTheme(action.id)
        is SelectTheme -> viewModel.select(action.id)
        is SetUseSystemDefault -> viewModel.setUseSystemDefault(action.value)
        is SetLightTheme -> viewModel.setLightTheme(action.value)
        is SetDarkTheme -> viewModel.setDarkTheme(action.value)
      }
    },
  )
}

@Composable
private fun ThemeSettingsScaffold(
  state: ThemeSettingsState,
  onAction: OnAction<ThemeSettingsAction>,
  highlight: SettingsHighlight? = null,
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
        LightThemePreference(
          preference = state.dayTheme,
          customThemeNames = state.customThemeNames,
          onAction = onAction,
        )
        DarkThemePreference(
          preference = state.darkTheme,
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
          dayTheme =
            ListPreference(
              value = LightColors.id,
              options = persistentListOf(LightColors.id, PreviewLocalA),
              enabled = this,
            ),
          darkTheme =
            ListPreference(
              value = MidnightColors.id,
              options = persistentListOf(DarkColors.id, MidnightColors.id),
              enabled = this,
            ),
          constantTheme = LightColors.id,
          localThemes =
            persistentListOf(
              LocalTheme(PreviewLocalA, "Light (copy)", LightColors.swatch()),
              LocalTheme(PreviewLocalB, "Midnight with stripes", MidnightColors.swatch()),
            ),
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

private val PreviewLocalA = ThemeId("@local/a")
private val PreviewLocalB = ThemeId("@local/b")
