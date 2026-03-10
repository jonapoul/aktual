package aktual.settings.ui.root

import aktual.core.icons.material.ArrowRight
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.ThemeRoutine
import aktual.core.l10n.Strings
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedBooleanParameters
import aktual.core.ui.ThemedParams
import aktual.settings.ui.BasicPreferenceItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun ThemeSettingsItem(onClick: () -> Unit, modifier: Modifier = Modifier) {
  BasicPreferenceItem(
    modifier = modifier,
    title = Strings.settingsTheme,
    subtitle = null,
    icon = MaterialIcons.ThemeRoutine,
    onClick = onClick,
    rightContent = {
      NormalIconButton(
        contentDescription = Strings.settingsTheme,
        imageVector = MaterialIcons.ArrowRight,
        onClick = onClick,
      )
    },
  )
}

@Preview
@Composable
private fun PreviewThemeSettingsItem(
  @PreviewParameter(ThemedBooleanParameters::class) params: ThemedParams<Boolean>
) = PreviewWithColorScheme(params.theme) { ThemeSettingsItem(onClick = {}) }
