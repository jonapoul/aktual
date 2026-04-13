package aktual.prefs.ui.theme

import aktual.app.nav.BackNavigator
import aktual.app.nav.CustomThemesNavigator
import aktual.app.nav.InspectThemeNavigator
import aktual.core.l10n.Strings
import aktual.core.theme.DarkTheme
import aktual.core.theme.LightTheme
import aktual.core.theme.LocalTheme
import aktual.core.theme.MidnightTheme
import aktual.core.theme.Theme
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.Dimens
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.ThemeParameters
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.blurredTopBarContentPadding
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.theme.ThemeSettingsState
import aktual.prefs.vm.theme.ThemeSettingsViewModel
import androidx.compose.foundation.layout.Arrangement
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

@Composable
fun ThemeSettingsScreen(
  back: BackNavigator,
  toCustomThemes: CustomThemesNavigator,
  toInspectTheme: InspectThemeNavigator,
  viewModel: ThemeSettingsViewModel = metroViewModel<ThemeSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  ThemeSettingsScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        ThemeSettingsAction.NavBack -> back()
        ThemeSettingsAction.NavCustomThemes -> toCustomThemes()
        is ThemeSettingsAction.InspectTheme -> toInspectTheme(action.id)
        is ThemeSettingsAction.SelectTheme -> viewModel.select(action.id)
        is ThemeSettingsAction.SetUseSystemDefault -> viewModel.setUseSystemDefault(action.value)
        is ThemeSettingsAction.SetDarkTheme -> viewModel.setDarkTheme(action.value)
      }
    },
  )
}

@Composable
private fun ThemeSettingsScaffold(
  state: ThemeSettingsState,
  onAction: (ThemeSettingsAction) -> Unit,
) {
  val theme = LocalTheme.current
  val listState = rememberLazyListState()
  val blurState = rememberBlurredTopBarState()

  Scaffold(
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, isScrolled = listState.canScrollBackward),
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(ThemeSettingsAction.NavBack) } },
        title = { Text(Strings.settingsThemeToolbar) },
      )
    }
  ) { innerPadding ->
    ThemeSettingsContent(
      modifier = Modifier.blurredTopBarContent(blurState, innerPadding),
      contentPadding = blurredTopBarContentPadding(blurState, innerPadding),
      state = state,
      listState = listState,
      onAction = onAction,
    )
  }
}

@Composable
private fun ThemeSettingsContent(
  state: ThemeSettingsState,
  listState: LazyListState,
  contentPadding: PaddingValues,
  onAction: (ThemeSettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState).padding(Dimens.Large),
    state = listState,
    contentPadding = contentPadding,
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

    item { CustomThemesPreference(enabled = !state.useSystemDefault.value, onAction = onAction) }

    item {
      BottomStatusBarSpacing()
      BottomNavBarSpacing()
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewThemeSettings(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    ThemeSettingsScaffold(
      state =
        ThemeSettingsState(
          useSystemDefault = BooleanPreference(value = false, enabled = true),
          darkTheme =
            ListPreference(
              value = DarkTheme.id,
              options = persistentListOf(DarkTheme.id, MidnightTheme.id),
              enabled = false,
            ),
          constantTheme = LightTheme.id,
        ),
      onAction = {},
    )
  }
