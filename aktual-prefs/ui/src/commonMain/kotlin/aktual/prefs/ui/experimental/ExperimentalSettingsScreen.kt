package aktual.prefs.ui.experimental

import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.theme.Colors
import aktual.core.ui.ColorParameters
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.vm.experimental.ExperimentalSettingsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun ExperimentalSettingsScreen(
  back: BackNavigator,
  @Suppress("UnusedParameter") // Nothing to show yet
  viewModel: ExperimentalSettingsViewModel = metroViewModel<ExperimentalSettingsViewModel>(),
) {
  ExperimentalSettingsScaffold(onBack = { back() })
}

@Composable
private fun ExperimentalSettingsScaffold(onBack: () -> Unit) {
  SettingsListScaffold(
    title = Strings.settingsExperimental,
    description = Strings.settingsExperimentalDesc,
    onBack = onBack,
  ) {}
}

@PortraitPreview
@Composable
private fun PreviewExperimentalSettingsScaffold(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) = PreviewWithColors(colors) { ExperimentalSettingsScaffold(onBack = {}) }
