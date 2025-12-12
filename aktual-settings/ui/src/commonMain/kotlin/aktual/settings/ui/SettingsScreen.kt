package aktual.settings.ui

import aktual.core.model.DarkColorSchemeType
import aktual.core.model.RegularColorSchemeType
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.Dimens
import aktual.core.ui.LocalTheme
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.WavyBackground
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.l10n.Strings
import aktual.settings.ui.items.ShowBottomBarPreferenceItem
import aktual.settings.ui.items.ThemePreferenceItem
import aktual.settings.vm.PreferenceValue
import aktual.settings.vm.SettingsViewModel
import aktual.settings.vm.ThemeConfig
import alakazam.kotlin.compose.VerticalSpacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun SettingsScreen(
  nav: SettingsNavigator,
  viewModel: SettingsViewModel = metroViewModel<SettingsViewModel>(),
) {
  val values by viewModel.prefValues.collectAsStateWithLifecycle()

  SettingsScaffold(
    values = values,
    onAction = { action ->
      when (action) {
        SettingsAction.NavBack -> nav.back()
        is SettingsAction.PreferenceChange -> viewModel.set(action.value)
      }
    },
  )
}

@Composable
internal fun SettingsScaffold(
  values: ImmutableList<PreferenceValue>,
  onAction: (SettingsAction) -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
  val theme = LocalTheme.current

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = {
          IconButton(onClick = { onAction(SettingsAction.NavBack) }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = Strings.navBack,
            )
          }
        },
        title = { Text(Strings.settingsToolbar) },
        scrollBehavior = scrollBehavior,
      )
    },
  ) { innerPadding ->
    Box {
      val hazeState = remember { HazeState() }

      WavyBackground(
        modifier = Modifier.hazeSource(hazeState),
      )

      SettingsContent(
        modifier = Modifier.padding(innerPadding),
        values = values,
        hazeState = hazeState,
        onAction = onAction,
      )
    }
  }
}

@Stable
@Composable
private fun SettingsContent(
  values: ImmutableList<PreferenceValue>,
  hazeState: HazeState,
  onAction: (SettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(Dimens.Large)
      .scrollbar(listState),
    state = listState,
  ) {
    itemsIndexed(values) { i, value ->
      PreferenceItem(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        hazeState = hazeState,
        onChange = { onAction(SettingsAction.PreferenceChange(it)) },
      )

      if (i != values.size - 1) {
        VerticalSpacer(10.dp)
      }
    }

    item {
      BottomStatusBarSpacing()
      BottomNavBarSpacing()
    }
  }
}

@Composable
private fun PreferenceItem(
  value: PreferenceValue,
  hazeState: HazeState,
  onChange: (PreferenceValue) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier,
  ) {
    when (value) {
      is PreferenceValue.Theme -> ThemePreferenceItem(
        config = value.config,
        hazeState = hazeState,
        onChange = { onChange(PreferenceValue.Theme(it)) },
      )

      is PreferenceValue.ShowBottomBar -> ShowBottomBarPreferenceItem(
        value = value.show,
        hazeState = hazeState,
        onChange = { onChange(PreferenceValue.ShowBottomBar(it)) },
      )
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewSettingsScaffold(
  @PreviewParameter(SettingsScaffoldProvider::class) params: ThemedParams<List<PreferenceValue>>,
) = PreviewWithColorScheme(params.type) {
  SettingsScaffold(
    onAction = {},
    values = params.data.toImmutableList(),
  )
}

private class SettingsScaffoldProvider : ThemedParameterProvider<List<PreferenceValue>>(
  listOf(
    PreferenceValue.Theme(ThemeConfig(RegularColorSchemeType.Dark, DarkColorSchemeType.Dark)),
    PreferenceValue.ShowBottomBar(show = false),
  ),
  listOf(
    PreferenceValue.Theme(ThemeConfig(RegularColorSchemeType.Light, DarkColorSchemeType.Dark)),
    PreferenceValue.ShowBottomBar(show = true),
  ),
)
