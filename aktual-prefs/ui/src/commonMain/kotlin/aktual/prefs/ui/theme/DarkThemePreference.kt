package aktual.prefs.ui.theme

import aktual.core.icons.AktualIcons
import aktual.core.icons.MoonStars
import aktual.core.l10n.Strings
import aktual.core.model.ThemeId
import aktual.core.theme.DarkColors
import aktual.core.theme.MidnightColors
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColorParams
import aktual.prefs.components.BasicPreferenceItem
import aktual.prefs.vm.ListPreference
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
  onAction: OnAction<ThemeSettingsAction>,
  modifier: Modifier = Modifier,
) {
  BasicPreferenceItem(
    modifier = modifier,
    title = Strings.settingsDarkTheme,
    subtitle = null,
    icon = AktualIcons.MoonStars,
    enabled = preference.enabled,
    onClick = null,
    rightContent = {
      AktualExposedDropDownMenu(
        modifier = Modifier.padding(end = 6.dp),
        value = preference.value,
        onValueChange = { id -> onAction(SetDarkTheme(id)) },
        options = preference.options,
        isEnabled = preference.enabled,
        string = { id ->
          when (id) {
            DarkColors.id -> Strings.settingsThemeDark
            MidnightColors.id -> Strings.settingsThemeMidnight
            else -> error("No other IDs supported: $id")
          }
        },
      )
    },
  )
}

@Preview
@Composable
private fun PreviewDarkThemePreference(
  @PreviewParameter(DarkThemePreferenceProvider::class)
  params: ColorParams<DarkThemePreferenceParams>,
) =
  PreviewWithColorParams(params) {
    DarkThemePreference(
      onAction = {},
      preference =
        ListPreference(
          value = selected,
          options = persistentListOf(DarkColors.id, MidnightColors.id),
          enabled = enabled,
        ),
    )
  }

private data class DarkThemePreferenceParams(val selected: ThemeId, val enabled: Boolean)

private class DarkThemePreferenceProvider :
  ColorParamsProvider<DarkThemePreferenceParams>(
    DarkThemePreferenceParams(selected = DarkColors.id, enabled = true),
    DarkThemePreferenceParams(selected = MidnightColors.id, enabled = true),
    DarkThemePreferenceParams(selected = MidnightColors.id, enabled = false),
  )
