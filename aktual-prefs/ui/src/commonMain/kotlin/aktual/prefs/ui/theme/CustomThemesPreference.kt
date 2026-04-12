package aktual.prefs.ui.theme

import aktual.core.icons.material.Build
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.PrimaryTextButton
import aktual.prefs.ui.BasicPreferenceItem
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun CustomThemesPreference(
  enabled: Boolean,
  onAction: (ThemeSettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  BasicPreferenceItem(
    modifier = modifier,
    title = Strings.settingsThemeCustomTitle,
    subtitle = null,
    icon = MaterialIcons.Build,
    enabled = enabled,
    onClick = { onAction(ThemeSettingsAction.NavCustomThemes) },
    bottomContent = {
      PrimaryTextButton(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        text = Strings.settingsThemeCustomButton,
        isEnabled = enabled,
        onClick = { onAction(ThemeSettingsAction.NavCustomThemes) },
      )
    },
  )
}
