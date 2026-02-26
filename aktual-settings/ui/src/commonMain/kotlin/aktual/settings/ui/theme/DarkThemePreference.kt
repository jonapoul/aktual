package aktual.settings.ui.theme

import aktual.core.icons.DarkMode
import aktual.core.icons.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.theme.DarkTheme
import aktual.core.theme.LocalTheme
import aktual.core.theme.MidnightTheme
import aktual.core.theme.Theme
import aktual.core.ui.CardShape
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.isTablet
import aktual.core.ui.segmentedButton
import aktual.settings.ui.BasicPreferenceItem
import aktual.settings.ui.NotClickable
import aktual.settings.vm.ListPreference
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonColors
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun DarkThemePreference(
  preference: ListPreference<Theme.Id>,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  buttonColors: SegmentedButtonColors = theme.segmentedButton(),
  isTablet: Boolean = isTablet(),
) {
  BasicPreferenceItem(
    modifier = modifier,
    title = Strings.settingsDarkTheme,
    subtitle = null,
    icon = MaterialIcons.DarkMode,
    enabled = preference.enabled,
    clickability = NotClickable,
    rightContent = { if (isTablet) DarkThemeContent(preference, buttonColors) },
    bottomContent = {
      if (!isTablet) {
        DarkThemeContent(
          modifier = Modifier.fillMaxWidth(),
          preference = preference,
          buttonColors = buttonColors,
        )
      }
    },
  )
}

@Composable
private fun DarkThemeContent(
  preference: ListPreference<Theme.Id>,
  buttonColors: SegmentedButtonColors,
  modifier: Modifier = Modifier,
) {
  SingleChoiceSegmentedButtonRow(modifier = modifier.clip(CardShape).padding(10.dp)) {
    SegmentedButton(
      selected = preference.selected == DarkTheme.id,
      onClick = { preference.onValueChange(DarkTheme.id) },
      shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2, baseShape = CardShape),
      enabled = preference.enabled,
      colors = buttonColors,
      label = { Text(Strings.settingsThemeDark, color = LocalContentColor.current) },
    )

    SegmentedButton(
      selected = preference.selected == MidnightTheme.id,
      onClick = { preference.onValueChange(MidnightTheme.id) },
      shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2, baseShape = CardShape),
      enabled = preference.enabled,
      colors = buttonColors,
      label = { Text(Strings.settingsThemeMidnight, color = LocalContentColor.current) },
    )
  }
}

@Preview
@Composable
private fun PreviewDarkThemePreference(
  @PreviewParameter(DarkThemePreferenceProvider::class)
  params: ThemedParams<DarkThemePreferenceParams>
) =
  PreviewWithColorScheme(params.theme) {
    DarkThemePreference(
      preference =
        ListPreference(
          selected = params.data.selected,
          values = persistentListOf(DarkTheme.id, MidnightTheme.id),
          onValueChange = {},
          enabled = params.data.enabled,
        )
    )
  }

private data class DarkThemePreferenceParams(val selected: Theme.Id, val enabled: Boolean)

private class DarkThemePreferenceProvider :
  ThemedParameterProvider<DarkThemePreferenceParams>(
    DarkThemePreferenceParams(selected = DarkTheme.id, enabled = true),
    DarkThemePreferenceParams(selected = DarkTheme.id, enabled = false),
    DarkThemePreferenceParams(selected = MidnightTheme.id, enabled = true),
    DarkThemePreferenceParams(selected = MidnightTheme.id, enabled = false),
  )
