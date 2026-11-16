package aktual.prefs.ui.dialogs

import aktual.core.icons.material.BlurCircular
import aktual.core.icons.material.Dialogs
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Opacity
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.theme.Colors
import aktual.core.ui.ColorParameters
import aktual.core.ui.LocalHazeConfig
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.ui.PreviewSettingsScaffold
import aktual.prefs.ui.SliderPreferenceItem
import aktual.prefs.ui.rememberSliderDraft
import aktual.prefs.vm.BlurAlphaPreference
import aktual.prefs.vm.BlurRadiusPreference
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.dialogs.DialogSettingsState
import aktual.prefs.vm.dialogs.DialogSettingsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun DialogSettingsScreen(
  back: BackNavigator,
  highlight: SettingsHighlight?,
  viewModel: DialogSettingsViewModel = metroViewModel<DialogSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  DialogSettingsScaffold(state = state, highlight = highlight, onBack = back::invoke)
}

@Composable
private fun DialogSettingsScaffold(
  state: DialogSettingsState,
  onBack: () -> Unit,
  highlight: SettingsHighlight? = null,
) {
  val blurRadius = rememberSliderDraft(state.blurRadius)
  val blurAlpha = rememberSliderDraft(state.blurAlpha)
  val config =
    LocalHazeConfig.current.copy(
      dialogs = state.blurDialogs.value,
      dialogBlurRadius = blurRadius.floatValue.dp,
      dialogBlurAlpha = blurAlpha.floatValue,
    )

  PreviewSettingsScaffold(
    title = Strings.settingsUiDialogsTitle,
    description = Strings.settingsDialogsDesc,
    onBack = onBack,
    preview = { DialogsPreview(config) },
    highlight = highlight,
  ) {
    item {
      BooleanPreferenceItem(
        preference = state.blurDialogs,
        title = Strings.settingsUiBlurDialogs,
        subtitle = null,
        icon = MaterialIcons.Dialogs,
      )
    }
    item {
      SliderPreferenceItem(
        preference = state.blurRadius,
        title = Strings.settingsUiBlurRadius,
        subtitle = null,
        icon = MaterialIcons.BlurCircular,
        onValueChanging = { blurRadius.floatValue = it },
      )
    }
    item {
      SliderPreferenceItem(
        preference = state.blurAlpha,
        title = Strings.settingsUiBlurAlpha,
        subtitle = null,
        icon = MaterialIcons.Opacity,
        onValueChanging = { blurAlpha.floatValue = it },
      )
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewDialogSettingsScaffold(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    DialogSettingsScaffold(
      onBack = {},
      state =
        DialogSettingsState(
          blurDialogs = BooleanPreference(true),
          blurRadius = BlurRadiusPreference(5f),
          blurAlpha = BlurAlphaPreference(0.5f),
        ),
    )
  }
