package aktual.prefs.ui.theme

import aktual.core.icons.material.DarkMode
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.model.ThemeId
import aktual.core.theme.DarkTheme
import aktual.core.theme.MidnightTheme
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.SlidingToggleButton
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.isCompactWidth
import aktual.prefs.ui.BasicPreferenceItem
import aktual.prefs.vm.ListPreference
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun DarkThemePreference(
  preference: ListPreference<ThemeId>,
  onAction: (ThemeSettingsAction) -> Unit,
  modifier: Modifier = Modifier,
  isCompact: Boolean = isCompactWidth(),
) {
  BasicPreferenceItem(
    modifier = modifier,
    title = Strings.settingsDarkTheme,
    subtitle = null,
    icon = MaterialIcons.DarkMode,
    enabled = preference.enabled,
    onClick = null,
    rightContent = { if (!isCompact) DarkThemeContent(preference, onAction) },
    bottomContent = {
      if (isCompact) {
        DarkThemeContent(
          modifier = Modifier.fillMaxWidth(),
          preference = preference,
          onAction = onAction,
        )
      }
    },
  )
}

@Composable
private fun DarkThemeContent(
  preference: ListPreference<ThemeId>,
  onAction: (ThemeSettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  SlidingToggleButton(
    modifier = modifier.padding(10.dp),
    options = preference.options,
    selected = preference.value,
    isEnabled = preference.enabled,
    onSelect = { id -> onAction(ThemeSettingsAction.SetDarkTheme(id)) },
    string = { id ->
      when (id) {
        DarkTheme.id -> Strings.settingsThemeDark
        MidnightTheme.id -> Strings.settingsThemeMidnight
        else -> error("No other IDs supported: $id")
      }
    },
  )
}

@Preview
@Composable
private fun PreviewDarkThemePreference(
  @PreviewParameter(DarkThemePreferenceProvider::class)
  params: ThemedParams<DarkThemePreferenceParams>
) =
  PreviewWithTheme(params.theme) {
    DarkThemePreference(
      onAction = {},
      preference =
        ListPreference(
          value = params.data.selected,
          options = persistentListOf(DarkTheme.id, MidnightTheme.id),
          enabled = params.data.enabled,
        ),
    )
  }

private data class DarkThemePreferenceParams(val selected: ThemeId, val enabled: Boolean)

private class DarkThemePreferenceProvider :
  ThemedParameterProvider<DarkThemePreferenceParams>(
    DarkThemePreferenceParams(selected = DarkTheme.id, enabled = true),
    DarkThemePreferenceParams(selected = DarkTheme.id, enabled = false),
    DarkThemePreferenceParams(selected = MidnightTheme.id, enabled = true),
    DarkThemePreferenceParams(selected = MidnightTheme.id, enabled = false),
  )
