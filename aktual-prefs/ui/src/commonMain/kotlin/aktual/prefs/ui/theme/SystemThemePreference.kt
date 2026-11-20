package aktual.prefs.ui.theme

import aktual.core.icons.AktualIcons
import aktual.core.icons.MoonStars
import aktual.core.icons.Sun
import aktual.core.l10n.Strings
import aktual.core.model.ThemeId
import aktual.core.theme.DarkColors
import aktual.core.theme.LightColors
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@Composable
internal fun LightThemePreference(
  preference: ListPreference<ThemeId>,
  customThemeNames: ImmutableMap<ThemeId, String>,
  onAction: OnAction<ThemeSettingsAction>,
  modifier: Modifier = Modifier,
) =
  SystemThemePreference(
    modifier = modifier,
    title = Strings.settingsLightTheme,
    icon = AktualIcons.Sun,
    preference = preference,
    customThemeNames = customThemeNames,
    onSelect = { id -> onAction(SetLightTheme(id)) },
  )

@Composable
internal fun DarkThemePreference(
  preference: ListPreference<ThemeId>,
  customThemeNames: ImmutableMap<ThemeId, String>,
  onAction: OnAction<ThemeSettingsAction>,
  modifier: Modifier = Modifier,
) =
  SystemThemePreference(
    modifier = modifier,
    title = Strings.settingsDarkTheme,
    icon = AktualIcons.MoonStars,
    preference = preference,
    customThemeNames = customThemeNames,
    onSelect = { id -> onAction(SetDarkTheme(id)) },
  )

@Composable
private fun SystemThemePreference(
  title: String,
  icon: ImageVector,
  preference: ListPreference<ThemeId>,
  customThemeNames: ImmutableMap<ThemeId, String>,
  onSelect: (ThemeId) -> Unit,
  modifier: Modifier = Modifier,
) {
  BasicPreferenceItem(
    modifier = modifier,
    title = title,
    subtitle = null,
    icon = icon,
    enabled = preference.enabled,
    onClick = null,
    rightContent = {
      AktualExposedDropDownMenu(
        modifier = Modifier.padding(end = 6.dp),
        value = preference.value,
        onValueChange = onSelect,
        options = preference.options,
        isEnabled = preference.enabled,
        string = { id -> themeName(id, customThemeNames) },
      )
    },
  )
}

@Composable
internal fun themeName(id: ThemeId, customThemeNames: ImmutableMap<ThemeId, String>): String =
  when (id) {
    LightColors.id -> Strings.settingsThemeLight
    DarkColors.id -> Strings.settingsThemeDark
    MidnightColors.id -> Strings.settingsThemeMidnight
    else -> customThemeNames[id] ?: id.value
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
      customThemeNames = persistentMapOf(PreviewCoffee to "Shades of Coffee"),
      preference =
        ListPreference(
          value = selected,
          options = persistentListOf(DarkColors.id, MidnightColors.id, PreviewCoffee),
          enabled = enabled,
        ),
    )
  }

private data class DarkThemePreferenceParams(val selected: ThemeId, val enabled: Boolean)

private class DarkThemePreferenceProvider :
  ColorParamsProvider<DarkThemePreferenceParams>(
    DarkThemePreferenceParams(selected = DarkColors.id, enabled = true),
    DarkThemePreferenceParams(selected = PreviewCoffee, enabled = true),
    DarkThemePreferenceParams(selected = MidnightColors.id, enabled = false),
  )

private val PreviewCoffee = ThemeId("user/coffee")
