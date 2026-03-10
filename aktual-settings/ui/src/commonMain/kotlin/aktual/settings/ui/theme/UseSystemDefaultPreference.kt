package aktual.settings.ui.theme

import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.ThemeRoutine
import aktual.core.l10n.Strings
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedBooleanParameters
import aktual.core.ui.ThemedParams
import aktual.settings.ui.BooleanPreferenceItem
import aktual.settings.vm.BooleanPreference
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun UseSystemDefaultPreference(
  preference: BooleanPreference,
  onAction: (ThemeSettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  BooleanPreferenceItem(
    modifier = modifier.fillMaxWidth(),
    value = preference.value,
    onValueChange = { value -> onAction(ThemeSettingsAction.SetUseSystemDefault(value)) },
    title = Strings.settingsThemeUseSystem,
    subtitle = null,
    icon = MaterialIcons.ThemeRoutine,
    enabled = preference.enabled,
  )
}

@Preview
@Composable
private fun PreviewUseSystemDefaultPreference(
  @PreviewParameter(ThemedBooleanParameters::class) params: ThemedParams<Boolean>
) =
  PreviewWithColorScheme(params.theme) {
    UseSystemDefaultPreference(
      preference = BooleanPreference(value = params.data, enabled = true),
      onAction = {},
    )
  }
