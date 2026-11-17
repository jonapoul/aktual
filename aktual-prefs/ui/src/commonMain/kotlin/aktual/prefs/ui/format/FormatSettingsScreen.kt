package aktual.prefs.ui.format

import aktual.core.icons.AktualIcons
import aktual.core.icons.Location
import aktual.core.l10n.Strings
import aktual.core.model.DistanceUnit
import aktual.core.nav.BackNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.theme.Colors
import aktual.core.ui.ColorParameters
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.prefs.components.PreferenceGroup
import aktual.prefs.ui.ListPreferenceItem
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.format.FormatSettingsState
import aktual.prefs.vm.format.FormatSettingsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun FormatSettingsScreen(
  back: BackNavigator,
  highlight: SettingsHighlight?,
  viewModel: FormatSettingsViewModel = metroViewModel<FormatSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  FormatSettingsScaffold(
    state = state,
    highlight = highlight,
    onBack = { back() },
  )
}

@Composable
private fun FormatSettingsScaffold(
  state: FormatSettingsState,
  onBack: () -> Unit,
  highlight: SettingsHighlight? = null,
) {
  SettingsListScaffold(
    title = Strings.settingsFormatGroup,
    description = Strings.settingsFormatDesc,
    highlight = highlight,
    onBack = onBack,
  ) {
    item {
      PreferenceGroup(title = Strings.settingsFormatDistanceGroup) {
        ListPreferenceItem(
          preference = state.distanceUnit,
          optionString = { u -> u.label() },
          optionSuffix = null,
          title = Strings.settingsFormatDistance,
          subtitle = null,
          icon = AktualIcons.Location,
        )
      }
    }
  }
}

@Composable
private fun DistanceUnit.label(): String =
  when (this) {
    Metric -> Strings.settingsFormatDistanceMetric
    Imperial -> Strings.settingsFormatDistanceImperial
  }

@PortraitPreview
@Composable
private fun PreviewFormatSettingsScaffold(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    FormatSettingsScaffold(
      onBack = {},
      state = FormatSettingsState(distanceUnit = ListPreference(Default)),
    )
  }
