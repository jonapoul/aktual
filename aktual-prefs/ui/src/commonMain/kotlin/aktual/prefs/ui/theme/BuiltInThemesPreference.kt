package aktual.prefs.ui.theme

import aktual.core.icons.material.ArrowRight
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.model.ThemeId
import aktual.core.theme.DarkTheme
import aktual.core.theme.LightTheme
import aktual.core.theme.LocalTheme
import aktual.core.theme.MidnightTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.CardShape
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.ThemeParameters
import aktual.core.ui.disabledIf
import aktual.core.ui.radioButton
import aktual.prefs.ui.BasicPreferenceItem
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun BuiltInThemesPreference(
  selectedTheme: ThemeId?,
  enabled: Boolean,
  onAction: (ThemeSettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  BasicPreferenceItem(
    modifier = modifier,
    title = Strings.settingsThemeBuiltIn,
    subtitle = null,
    icon = null,
    enabled = enabled,
    onClick = null,
    bottomContent = {
      Column(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        BuiltInThemeItem(
          id = LightTheme.id,
          name = Strings.settingsThemeLight,
          selectedId = selectedTheme,
          enabled = enabled,
          onAction = onAction,
        )
        BuiltInThemeItem(
          id = DarkTheme.id,
          name = Strings.settingsThemeDark,
          selectedId = selectedTheme,
          enabled = enabled,
          onAction = onAction,
        )
        BuiltInThemeItem(
          id = MidnightTheme.id,
          name = Strings.settingsThemeMidnight,
          selectedId = selectedTheme,
          enabled = enabled,
          onAction = onAction,
        )
      }
    },
  )
}

@Composable
private fun BuiltInThemeItem(
  id: ThemeId,
  name: String,
  selectedId: ThemeId?,
  enabled: Boolean,
  onAction: (ThemeSettingsAction) -> Unit,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier = Modifier.height(IntrinsicSize.Min),
    horizontalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    val isSelected = selectedId == id
    val backgroundColor =
      remember(theme, isSelected) {
        theme.buttonNormalBackground.disabledIf(enabled || !isSelected)
      }
    Row(
      modifier =
        Modifier.fillMaxHeight().weight(1f).background(backgroundColor, CardShape).clickable(
          enabled
        ) {
          onAction(ThemeSettingsAction.SelectTheme(id))
        },
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      RadioButton(
        modifier = Modifier.padding(8.dp),
        enabled = enabled,
        selected = isSelected,
        onClick = null,
        colors = theme.radioButton(),
      )

      Text(
        modifier = Modifier.weight(1f),
        text = name,
        style = AktualTypography.bodyLarge,
        color = theme.buttonNormalText.disabledIf(!enabled),
      )
    }

    NormalIconButton(
      modifier = Modifier.clip(CardShape),
      imageVector = MaterialIcons.ArrowRight,
      enabled = enabled,
      onClick = { onAction(ThemeSettingsAction.InspectTheme(id)) },
      contentDescription = Strings.settingsThemePreview(name),
    )
  }
}

@Preview
@Composable
private fun PreviewBuiltInThemesPreference(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    BuiltInThemesPreference(selectedTheme = LightTheme.id, enabled = true, onAction = {})
  }

@Preview
@Composable
private fun PreviewBuiltInThemesPreferenceDisabled(
  @PreviewParameter(ThemeParameters::class) theme: Theme
) =
  PreviewWithTheme(theme) {
    BuiltInThemesPreference(selectedTheme = LightTheme.id, enabled = false, onAction = {})
  }
