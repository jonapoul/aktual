package aktual.settings.ui.root

import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.Dimens
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.settings.vm.BooleanPreference
import aktual.settings.vm.root.SettingsScreenState
import aktual.settings.vm.root.SettingsViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun SettingsScreen(
  nav: SettingsNavigator,
  viewModel: SettingsViewModel = metroViewModel<SettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  SettingsScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        SettingsAction.NavBack -> nav.back()
        SettingsAction.NavToThemeSettings -> nav.toThemeSettings()
        is SettingsAction.SetShowBottomBar -> viewModel.showBottomBar(action.value)
      }
    },
  )
}

@Composable
private fun SettingsScaffold(state: SettingsScreenState, onAction: (SettingsAction) -> Unit) {
  val theme = LocalTheme.current

  Scaffold(
    topBar = {
      TopAppBar(
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(SettingsAction.NavBack) } },
        title = { Text(Strings.settingsToolbar) },
      )
    }
  ) { innerPadding ->
    SettingsContent(modifier = Modifier.padding(innerPadding), state = state, onAction = onAction)
  }
}

@Composable
private fun SettingsContent(
  state: SettingsScreenState,
  onAction: (SettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(Dimens.Large).scrollbar(listState),
    state = listState,
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item { ShowBottomBarPreferenceItem(state.showBottomBar, onAction) }

    item { ThemeSettingsItem(onClick = { onAction(SettingsAction.NavToThemeSettings) }) }

    item {
      BottomStatusBarSpacing()
      BottomNavBarSpacing()
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewSettingsScaffold(
  @PreviewParameter(SettingsScaffoldProvider::class) params: ThemedParams<SettingsScreenState>
) = PreviewWithColorScheme(params.theme) { SettingsScaffold(onAction = {}, state = params.data) }

private class SettingsScaffoldProvider :
  ThemedParameterProvider<SettingsScreenState>(
    SettingsScreenState(showBottomBar = BooleanPreference(value = true)),
    SettingsScreenState(showBottomBar = BooleanPreference(value = false)),
  )
