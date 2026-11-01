package aktual.prefs.ui.systemui

import aktual.budget.model.BarEffect
import aktual.budget.model.BarGlassStyle
import aktual.core.icons.AktualIcons
import aktual.core.icons.ViewHide
import aktual.core.icons.ViewShow
import aktual.core.icons.material.BlurCircular
import aktual.core.icons.material.BlurOn
import aktual.core.icons.material.Dialogs
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Opacity
import aktual.core.icons.material.RoundedCorner
import aktual.core.icons.material.Security
import aktual.core.icons.material.Texture
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.theme.Colors
import aktual.core.ui.ColorParameters
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.ui.ListPreferenceItem
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.ui.SliderPreferenceItem
import aktual.prefs.ui.core.PreferenceGroup
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.systemui.GlassCornerRadiusPreference
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
        icon = if (state.showStatusBar.value) AktualIcons.ViewShow else AktualIcons.ViewHide,
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
    if (state.showBlurGroup) {
      item {
        PreferenceGroup(title = Strings.settingsUiBlurTitle) {
          SliderPreferenceItem(
            preference = state.hazeRadiusDp,
            title = Strings.settingsUiBlurRadius,
            subtitle = null,
            icon = MaterialIcons.BlurCircular,
          )
          SliderPreferenceItem(
            preference = state.hazeAlpha,
            title = Strings.settingsUiBlurAlpha,
            subtitle = null,
            icon = MaterialIcons.Opacity,
          )
        }
      }
    }
    if (state.showGlassGroup) {
      item {
        PreferenceGroup(title = Strings.settingsUiGlassTitle) {
          ListPreferenceItem(
            preference = state.glassStyle,
            optionString = { it.string() },
            optionSuffix = null,
            title = Strings.settingsUiGlassStyle,
            subtitle = null,
            icon = MaterialIcons.Texture,
          )
          SliderPreferenceItem(
            preference = state.glassCornerRadiusDp,
            title = Strings.settingsUiGlassCornerRadius,
            subtitle = null,
            icon = MaterialIcons.RoundedCorner,
          )
        }
      }
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

@Composable
private fun BarGlassStyle.string(): String =
  when (this) {
    Regular -> Strings.settingsUiGlassStyleRegular
    Clear -> Strings.settingsUiGlassStyleClear
  }

@PortraitPreview
@Composable
private fun PreviewSystemUiSettingsScaffold(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    SystemUiSettingsScaffold(
      onBack = {},
      state =
        SystemUiSettingsState(
          showStatusBar = BooleanPreference(true),
          appBarEffect = ListPreference(Blur),
          hazeDialogs = BooleanPreference(true),
          hazeRadiusDp = HazeRadiusPreference(5f),
          hazeAlpha = HazeAlphaPreference(0.5f),
          glassStyle = ListPreference(Regular),
          glassCornerRadiusDp = GlassCornerRadiusPreference(24f),
          hidePreviewInAppSwitcher = BooleanPreference(true),
        ),
    )
  }
