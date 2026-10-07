package aktual.prefs.ui.schedules

import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Visibility
import aktual.core.icons.material.VisibilityOff
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.Dimens
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.schedules.ScheduleSettingsState
import aktual.prefs.vm.schedules.ScheduleSettingsViewModel
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

@Composable
fun ScheduleSettingsScreen(
  back: BackNavigator,
  viewModel: ScheduleSettingsViewModel = metroViewModel<ScheduleSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  ScheduleSettingsScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        NavBack -> back()
      }
    },
  )
}

@Composable
private fun ScheduleSettingsScaffold(
  state: ScheduleSettingsState,
  onAction: ScheduleSettingsActionHandler,
) {
  val listState = rememberLazyListState()
  val hazeState = rememberHazedTopBarState()

  Scaffold(
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(NavBack) } },
        title = { Text(Strings.settingsSchedulesToolbar) },
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()
      ScheduleSettingsContent(
        modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
        contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
        listState = listState,
        state = state,
      )
    }
  }
}

@Composable
private fun ScheduleSettingsContent(
  state: ScheduleSettingsState,
  contentPadding: PaddingValues,
  listState: LazyListState,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState).padding(Dimens.Large),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item {
      BooleanPreferenceItem(
        preference = state.showCompleted,
        title = Strings.settingsSchedulesShowCompleted,
        subtitle = null,
        icon =
          if (state.showCompleted.value) MaterialIcons.Visibility else MaterialIcons.VisibilityOff,
      )
    }
    item { BottomSpacing() }
  }
}

@PortraitPreview
@Composable
private fun PreviewScheduleSettingsScaffold(
  @PreviewParameter(ScheduleSettingsProvider::class) params: ColoredParams<ScheduleSettingsState>,
) = PreviewWithColoredParams(params) { ScheduleSettingsScaffold(state = this, onAction = {}) }

private class ScheduleSettingsProvider :
  ColoredParameterProvider<ScheduleSettingsState>(
    ScheduleSettingsState(showCompleted = BooleanPreference(value = true)),
    ScheduleSettingsState(showCompleted = BooleanPreference(value = false)),
  )
