package actual.preview

import actual.core.model.DarkColorSchemeType
import actual.core.model.RegularColorSchemeType
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
import actual.settings.ui.SettingsScaffold
import actual.settings.vm.PreferenceValue
import actual.settings.vm.ThemeConfig
import androidx.compose.runtime.Composable
import kotlinx.collections.immutable.persistentListOf

@TripleScreenPreview
@Composable
private fun Regular() = PreviewThemedScreen { type ->
  SettingsScaffold(
    onAction = {},
    values = persistentListOf(
      PreferenceValue.Theme(ThemeConfig(RegularColorSchemeType.Dark, DarkColorSchemeType.Dark)),
      PreferenceValue.ShowBottomBar(false),
    ),
  )
}
