package aktual.prefs.ui.theme

import aktual.core.icons.AktualIcons
import aktual.core.icons.MoonStars
import aktual.core.l10n.Strings
import aktual.core.model.ThemeId
import aktual.core.theme.DarkColors
import aktual.core.theme.MidnightColors
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.ColorParams
import aktual.core.ui.DesktopPreview
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.isCompactWidth
import aktual.prefs.ui.core.BasicPreferenceItem
import aktual.prefs.vm.ListPreference
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun DarkThemePreference(
  preference: ListPreference<ThemeId>,
  onAction: ThemeSettingsActionHandler,
  modifier: Modifier = Modifier,
  isCompact: Boolean = isCompactWidth(),
) {
  BasicPreferenceItem(
    modifier = modifier,
    title = Strings.settingsDarkTheme,
    subtitle = null,
    icon = AktualIcons.MoonStars,
    enabled = preference.enabled,
    onClick = null,
    rightContent = {
      if (!isCompact) {
        DarkThemeContent(
          modifier = Modifier.width(ToggleWidth),
          preference = preference,
          onAction = onAction,
        )
      }
    },
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

private val ToggleWidth = 300.dp

@Composable
private fun DarkThemeContent(
  preference: ListPreference<ThemeId>,
  onAction: ThemeSettingsActionHandler,
  modifier: Modifier = Modifier,
) {
  AktualSlidingToggleButton(
    modifier = modifier.padding(10.dp),
    options = preference.options,
    selected = preference.value,
    isEnabled = preference.enabled,
    onSelect = { id -> onAction(SetDarkTheme(id)) },
    string = { id ->
      when (id) {
        DarkColors.id -> Strings.settingsThemeDark
        MidnightColors.id -> Strings.settingsThemeMidnight
        else -> error("No other IDs supported: $id")
      }
    },
  )
}

@PortraitPreview
@DesktopPreview
@Composable
private fun PreviewDarkColorsPreference(
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
    DarkThemePreferenceParams(selected = DarkColors.id, enabled = false),
    DarkThemePreferenceParams(selected = MidnightColors.id, enabled = true),
    DarkThemePreferenceParams(selected = MidnightColors.id, enabled = false),
  )
