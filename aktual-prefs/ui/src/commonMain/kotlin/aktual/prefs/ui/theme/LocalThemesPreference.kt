package aktual.prefs.ui.theme

import aktual.core.l10n.Strings
import aktual.core.model.ThemeId
import aktual.core.theme.Colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PreviewWithColors
import aktual.prefs.ui.core.BasicPreferenceItem
import aktual.prefs.vm.theme.LocalTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun LocalThemesPreference(
  themes: ImmutableList<LocalTheme>,
  selectedTheme: ThemeId?,
  enabled: Boolean,
  onAction: ThemeSettingsActionHandler,
  modifier: Modifier = Modifier,
) {
  BasicPreferenceItem(
    modifier = modifier,
    title = Strings.settingsThemeLocalTitle,
    subtitle = if (themes.isEmpty()) Strings.settingsThemeLocalEmpty else null,
    icon = null,
    enabled = enabled,
    onClick = null,
    bottomContent =
      if (themes.isEmpty()) {
        null
      } else {
        {
          Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            themes.fastForEach { theme ->
              ThemeItem(
                id = theme.id,
                name = theme.name,
                selectedId = selectedTheme,
                enabled = enabled,
                onAction = onAction,
              )
            }
          }
        }
      },
  )
}

@Preview
@Composable
private fun PreviewLocalThemesPreference(
  @PreviewParameter(ColoredParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    LocalThemesPreference(
      themes =
        persistentListOf(
          LocalTheme(ThemeId("@local/a"), "Light (copy)"),
          LocalTheme(ThemeId("@local/b"), "Midnight with stripes"),
        ),
      selectedTheme = ThemeId("@local/b"),
      enabled = true,
      onAction = {},
    )
  }

@Preview
@Composable
private fun PreviewLocalThemesPreferenceEmpty(
  @PreviewParameter(ColoredParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    LocalThemesPreference(
      themes = persistentListOf(),
      selectedTheme = null,
      enabled = true,
      onAction = {},
    )
  }
