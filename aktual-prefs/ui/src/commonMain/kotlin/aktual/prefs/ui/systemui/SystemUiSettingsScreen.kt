package aktual.prefs.ui.systemui

import aktual.budget.model.BarEffect
import aktual.core.icons.material.BlurCircular
import aktual.core.icons.material.BlurOn
import aktual.core.icons.material.Dialogs
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Opacity
import aktual.core.icons.material.Security
import aktual.core.icons.material.Visibility
import aktual.core.icons.material.VisibilityOff
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.theme.Colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.ui.ListPreferenceItem
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.ui.SliderPreferenceItem
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.systemui.HazeAlphaPreference
import aktual.prefs.vm.systemui.HazeRadiusPreference
import aktual.prefs.vm.systemui.SystemUiSettingsState
import aktual.prefs.vm.systemui.SystemUiSettingsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun SystemUiSettingsScreen(
  back: BackNavigator,
  viewModel: SystemUiSettingsViewModel = metroViewModel<SystemUiSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  SystemUiSettingsScaffold(state = state, onBack = { back() })
}

@Composable
private fun SystemUiSettingsScaffold(state: SystemUiSettingsState, onBack: () -> Unit) {
  SettingsListScaffold(title = Strings.settingsUiGroup, description = null, onBack = onBack) {
    item {
      BooleanPreferenceItem(
        preference = state.showStatusBar,
        title = Strings.settingsUiShowStatus,
        subtitle = null,
        icon =
          if (state.showStatusBar.value) MaterialIcons.Visibility else MaterialIcons.VisibilityOff,
      )
    }
    item {
      ListPreferenceItem(
        preference = state.appBarEffect,
        optionString = { it.string() },
        optionSuffix = null,
        title = Strings.settingsUiBarEffect,
        subtitle = null,
        icon = MaterialIcons.BlurOn,
      )
    }
    item {
      BooleanPreferenceItem(
        preference = state.hazeDialogs,
        title = Strings.settingsUiBlurDialogs,
        subtitle = null,
        icon = MaterialIcons.Dialogs,
      )
    }
    item {
      SliderPreferenceItem(
        preference = state.hazeRadiusDp,
        title = Strings.settingsUiBlurRadius,
        subtitle = null,
        icon = MaterialIcons.BlurCircular,
      )
    }
    item {
      SliderPreferenceItem(
        preference = state.hazeAlpha,
        title = Strings.settingsUiBlurAlpha,
        subtitle = null,
        icon = MaterialIcons.Opacity,
      )
    }
    if (state.hidePreviewInAppSwitcher.visible) {
      item {
        BooleanPreferenceItem(
          preference = state.hidePreviewInAppSwitcher,
          title = Strings.settingsUiHidePreview,
          subtitle = Strings.settingsUiHidePreviewDesc,
          icon = MaterialIcons.Security,
        )
      }
    }
  }
}

@Composable
private fun BarEffect.string(): String =
  when (this) {
    None -> Strings.settingsUiBarEffectNone
    Blur -> Strings.settingsUiBarEffectBlur
    Glass -> Strings.settingsUiBarEffectGlass
  }

@PortraitPreview
@Composable
private fun PreviewSystemUiSettingsScaffold(
  @PreviewParameter(ColoredParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    SystemUiSettingsScaffold(
      onBack = {},
      state =
        SystemUiSettingsState(
          showStatusBar = BooleanPreference(true),
          appBarEffect = ListPreference(BarEffect.Blur),
          hazeDialogs = BooleanPreference(true),
          hazeRadiusDp = HazeRadiusPreference(5f),
          hazeAlpha = HazeAlphaPreference(0.5f),
          hidePreviewInAppSwitcher = BooleanPreference(true),
        ),
    )
  }
