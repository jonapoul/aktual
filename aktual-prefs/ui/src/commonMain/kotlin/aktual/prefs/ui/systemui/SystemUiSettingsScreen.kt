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
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.ColorParameters
import aktual.core.ui.Dimens
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.PrimaryTextButton
import aktual.prefs.components.PreferenceGroup
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.ui.ListPreferenceItem
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.ui.SliderPreferenceItem
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.SliderPreference
import aktual.prefs.vm.systemui.BlurAlphaPreference
import aktual.prefs.vm.systemui.BlurRadiusPreference
import aktual.prefs.vm.systemui.GlassCornerRadiusPreference
import aktual.prefs.vm.systemui.SystemUiSettingsState
import aktual.prefs.vm.systemui.SystemUiSettingsViewModel
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    item {
      PreferenceGroup(title = Strings.settingsUiBarsTitle, subtitle = Strings.settingsUiBarsDesc) {
        BooleanPreferenceItem(
          preference = state.showStatusBar,
          title = Strings.settingsUiShowStatus,
          subtitle = null,
          icon = if (state.showStatusBar.value) AktualIcons.ViewShow else AktualIcons.ViewHide,
        )
        ListPreferenceItem(
          preference = state.barEffect,
          optionString = { it.string() },
          optionSuffix = null,
          title = Strings.settingsUiBarEffect,
          subtitle = null,
          icon = MaterialIcons.BlurOn,
        )
        BlurSettings(radius = state.barBlurRadius, alpha = state.barBlurAlpha)
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
    item {
      PreferenceGroup(title = Strings.settingsUiDialogsTitle) {
        BooleanPreferenceItem(
          preference = state.blurDialogs,
          title = Strings.settingsUiBlurDialogs,
          subtitle = null,
          icon = MaterialIcons.Dialogs,
        )
        BlurSettings(radius = state.dialogBlurRadius, alpha = state.dialogBlurAlpha)
        TestDialogButton()
      }
    }
  }
}

@Composable
private fun TestDialogButton() {
  var showDialog by remember { mutableStateOf(false) }
  PrimaryTextButton(
    modifier = Modifier.fillMaxWidth().padding(Dimens.Large),
    text = Strings.settingsUiTestDialog,
    onClick = { showDialog = true },
  )

  if (showDialog) {
    AktualAlertDialog(
      title = Strings.settingsUiTestDialogTitle,
      onDismissRequest = { showDialog = false },
      buttons = {
        TextButton(onClick = { showDialog = false }) {
          Text(Strings.settingsUiTestDialogDismiss)
        }
      },
    ) {
      Text(Strings.settingsUiTestDialogMessage)
    }
  }
}

@Composable
private fun BlurSettings(radius: SliderPreference, alpha: SliderPreference) {
  SliderPreferenceItem(
    preference = radius,
    title = Strings.settingsUiBlurRadius,
    subtitle = null,
    icon = MaterialIcons.BlurCircular,
  )
  SliderPreferenceItem(
    preference = alpha,
    title = Strings.settingsUiBlurAlpha,
    subtitle = null,
    icon = MaterialIcons.Opacity,
  )
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
          hidePreviewInAppSwitcher = BooleanPreference(true),
          showStatusBar = BooleanPreference(true),
          barEffect = ListPreference(Blur),
          barBlurRadius = BlurRadiusPreference(5f),
          barBlurAlpha = BlurAlphaPreference(0.5f),
          glassStyle = ListPreference(Regular, enabled = false),
          glassCornerRadiusDp = GlassCornerRadiusPreference(24f, enabled = false),
          blurDialogs = BooleanPreference(true),
          dialogBlurRadius = BlurRadiusPreference(5f),
          dialogBlurAlpha = BlurAlphaPreference(0.5f),
        ),
    )
  }
