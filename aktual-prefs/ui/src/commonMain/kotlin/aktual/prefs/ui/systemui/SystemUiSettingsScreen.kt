package aktual.prefs.ui.systemui

import aktual.core.icons.AktualIcons
import aktual.core.icons.ViewHide
import aktual.core.icons.ViewShow
import aktual.core.icons.material.BlurCircular
import aktual.core.icons.material.BlurOn
import aktual.core.icons.material.Dialogs
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Opacity
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.BarSettingsNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualButton
import aktual.core.ui.ColorParameters
import aktual.core.ui.DialogTextButton
import aktual.core.ui.Dimens
import aktual.core.ui.LocalHazeConfig
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.prefs.components.PreferenceGroup
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.ui.SliderPreferenceItem
import aktual.prefs.ui.bars.label
import aktual.prefs.ui.root.SubSettingsItem
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.SliderPreference
import aktual.prefs.vm.systemui.BlurAlphaPreference
import aktual.prefs.vm.systemui.BlurRadiusPreference
import aktual.prefs.vm.systemui.SystemUiSettingsState
import aktual.prefs.vm.systemui.SystemUiSettingsViewModel
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
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
  toBarSettings: BarSettingsNavigator,
  highlight: SettingsHighlight?,
  viewModel: SystemUiSettingsViewModel = metroViewModel<SystemUiSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  SystemUiSettingsScaffold(
    state = state,
    highlight = highlight,
    onBack = back::invoke,
    onOpenBars = { toBarSettings() },
  )
}

@Composable
private fun SystemUiSettingsScaffold(
  state: SystemUiSettingsState,
  onBack: () -> Unit,
  onOpenBars: () -> Unit,
  highlight: SettingsHighlight? = null,
) {
  SettingsListScaffold(
    title = Strings.settingsUiGroup,
    description = null,
    highlight = highlight,
    onBack = onBack,
  ) {
    item {
      BooleanPreferenceItem(
        preference = state.showStatusBar,
        title = Strings.settingsUiShowStatus,
        subtitle = null,
        icon = if (state.showStatusBar.value) AktualIcons.ViewShow else AktualIcons.ViewHide,
      )
    }
    item {
      SubSettingsItem(
        title = Strings.settingsUiBarsTitle,
        subtitle = LocalHazeConfig.current.appBarEffect.label(),
        icon = MaterialIcons.BlurOn,
        onClick = onOpenBars,
      )
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
  AktualButton(
    kind = Primary,
    modifier = Modifier.fillMaxWidth().padding(Dimens.Large),
    text = Strings.settingsUiTestDialog,
    onClick = { showDialog = true },
  )

  if (showDialog) {
    AktualAlertDialog(
      title = Strings.settingsUiTestDialogTitle,
      onDismissRequest = { showDialog = false },
      buttons = {
        DialogTextButton(
          text = Strings.settingsUiTestDialogDismiss,
          onClick = { showDialog = false },
        )
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

@PortraitPreview
@Composable
private fun PreviewSystemUiSettingsScaffold(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    SystemUiSettingsScaffold(
      onBack = {},
      onOpenBars = {},
      state =
        SystemUiSettingsState(
          showStatusBar = BooleanPreference(true),
          blurDialogs = BooleanPreference(true),
          dialogBlurRadius = BlurRadiusPreference(5f),
          dialogBlurAlpha = BlurAlphaPreference(0.5f),
        ),
    )
  }
