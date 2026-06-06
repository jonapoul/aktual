package aktual.prefs.ui.theme

import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.ThemeRoutine
import aktual.core.l10n.Strings
import aktual.core.ui.ColoredBooleanParameters
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColors
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.vm.BooleanPreference
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun UseSystemDefaultPreference(
  preference: BooleanPreference,
  onAction: ThemeSettingsActionHandler,
  modifier: Modifier = Modifier,
) {
  BooleanPreferenceItem(
    modifier = modifier.fillMaxWidth(),
    value = preference.value,
    onValueChange = { value -> onAction(SetUseSystemDefault(value)) },
    title = Strings.settingsThemeUseSystem,
    subtitle = null,
    icon = MaterialIcons.ThemeRoutine,
    enabled = preference.enabled,
  )
}

@Preview
@Composable
private fun PreviewUseSystemDefaultPreference(
  @PreviewParameter(ColoredBooleanParameters::class) params: ColoredParams<Boolean>
) =
  PreviewWithColors(params.colors) {
    UseSystemDefaultPreference(
      preference = BooleanPreference(value = params.data, enabled = true),
      onAction = {},
    )
  }
