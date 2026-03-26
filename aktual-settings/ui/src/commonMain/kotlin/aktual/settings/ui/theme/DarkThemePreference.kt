package aktual.settings.ui.theme

import aktual.core.icons.material.DarkMode
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.model.ThemeId
import aktual.core.theme.DarkTheme
import aktual.core.theme.LocalTheme
import aktual.core.theme.MidnightTheme
import aktual.core.theme.Theme
import aktual.core.ui.CardShape
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.isCompactWidth
import aktual.core.ui.segmentedButton
import aktual.settings.ui.BasicPreferenceItem
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
  preference: ListPreference<ThemeId>,
  onAction: (ThemeSettingsAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  buttonColors: SegmentedButtonColors = theme.segmentedButton(),
  isCompact: Boolean = isCompactWidth(),
) {
  BasicPreferenceItem(
    modifier = modifier,
    title = Strings.settingsDarkTheme,
    subtitle = null,
    icon = MaterialIcons.DarkMode,
    enabled = preference.enabled,
    onClick = null,
    rightContent = { if (!isCompact) DarkThemeContent(preference, buttonColors, onAction) },
    bottomContent = {
      if (isCompact) {
        DarkThemeContent(
          modifier = Modifier.fillMaxWidth(),
          preference = preference,
          buttonColors = buttonColors,
          onAction = onAction,
        )
      }
    },
  )
}

@Composable
private fun DarkThemeContent(
  preference: ListPreference<ThemeId>,
  buttonColors: SegmentedButtonColors,
  onAction: (ThemeSettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  SingleChoiceSegmentedButtonRow(modifier = modifier.clip(CardShape).padding(10.dp)) {
    SegmentedButton(
      selected = preference.value == DarkTheme.id,
      onClick = { onAction(ThemeSettingsAction.SetDarkTheme(DarkTheme.id)) },
      shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2, baseShape = CardShape),
      enabled = preference.enabled,
      colors = buttonColors,
      label = { Text(Strings.settingsThemeDark, color = LocalContentColor.current) },
    )

    SegmentedButton(
      selected = preference.value == MidnightTheme.id,
      onClick = { onAction(ThemeSettingsAction.SetDarkTheme(MidnightTheme.id)) },
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
