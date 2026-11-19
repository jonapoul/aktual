package aktual.prefs.ui.advanced

import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.theme.Colors
import aktual.core.ui.ColorParameters
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.vm.advanced.AdvancedSettingsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun AdvancedSettingsScreen(
  back: BackNavigator,
  @Suppress("UnusedParameter") // Nothing to show yet
  viewModel: AdvancedSettingsViewModel = metroViewModel<AdvancedSettingsViewModel>(),
) {
  AdvancedSettingsScaffold(onBack = { back() })
}

@Composable
private fun AdvancedSettingsScaffold(onBack: () -> Unit) {
  SettingsListScaffold(
    title = Strings.settingsAdvanced,
    description = null,
    onBack = onBack,
  ) {}
}

@PortraitPreview
@Composable
private fun PreviewAdvancedSettingsScaffold(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) = PreviewWithColors(colors) { AdvancedSettingsScaffold(onBack = {}) }
