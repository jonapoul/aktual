package aktual.prefs.ui.theme

import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronRight
import aktual.core.icons.CloudDownload
import aktual.core.l10n.Strings
import aktual.core.model.ThemeId
import aktual.core.theme.Colors
import aktual.core.theme.DarkColors
import aktual.core.theme.LightColors
import aktual.core.theme.MidnightColors
import aktual.core.ui.AktualIconButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.Chip
import aktual.core.ui.ColorParameters
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.RowShape
import aktual.core.ui.disabled
import aktual.core.ui.disabledIf
import aktual.core.ui.radioButton
import aktual.prefs.components.PreferenceGroup
import aktual.prefs.ui.root.SubSettingsItem
import aktual.prefs.vm.theme.LocalTheme
import aktual.prefs.vm.theme.ThemeSwatch
import aktual.prefs.vm.theme.swatch
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

// The catalog row stays enabled while matching the system, since browsing it changes nothing
@Composable
internal fun ThemesPreference(
  localThemes: ImmutableList<LocalTheme>,
  selectedTheme: ThemeId?,
  enabled: Boolean,
  onAction: OnAction<ThemeSettingsAction>,
  modifier: Modifier = Modifier,
) {
  PreferenceGroup(
    modifier = modifier,
    title = Strings.settingsThemeList,
    subtitle = Strings.settingsThemeListDesc,
  ) {
    BuiltInThemes.fastForEach { theme ->
      ThemeItem(
        id = theme.id,
        name = theme.name(),
        swatch = theme.swatch,
        isLocal = false,
        selectedId = selectedTheme,
        enabled = enabled,
        onAction = onAction,
      )
    }

    if (localThemes.isNotEmpty()) {
      ThemeDivider()
      localThemes.fastForEach { theme ->
        ThemeItem(
          id = theme.id,
          name = theme.name,
          swatch = theme.swatch,
          isLocal = true,
          selectedId = selectedTheme,
          enabled = enabled,
          onAction = onAction,
        )
      }
    }

    ThemeDivider()
    SubSettingsItem(
      title = Strings.settingsThemeCatalog,
      icon = AktualIcons.CloudDownload,
      onClick = { onAction(NavCustomThemes) },
    )
  }
}

private data class BuiltInTheme(val id: ThemeId, val swatch: ThemeSwatch)

@Composable
private fun BuiltInTheme.name(): String =
  when (id) {
    LightColors.id -> Strings.settingsThemeLight
    DarkColors.id -> Strings.settingsThemeDark
    MidnightColors.id -> Strings.settingsThemeMidnight
    else -> error("No other built-in themes: $id")
  }

private val BuiltInThemes: List<BuiltInTheme> by lazy {
  listOf(LightColors, DarkColors, MidnightColors).map { BuiltInTheme(it.id, it.swatch()) }
}

@Composable
private fun ThemeDivider() =
  HorizontalDivider(
    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
    color = colors.settingsCardBorder,
  )

@Composable
private fun ThemeItem(
  id: ThemeId,
  name: String,
  swatch: ThemeSwatch,
  isLocal: Boolean,
  selectedId: ThemeId?,
  enabled: Boolean,
  onAction: OnAction<ThemeSettingsAction>,
) {
  val isSelected = selectedId == id
  val isChosen = isSelected && enabled
  val backgroundColor by
    animateColorAsState(
      if (isChosen) colors.settingsNavItemBackgroundSelected else Color.Transparent,
    )

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 52.dp)
        .clip(RowShape)
        .background(backgroundColor, RowShape)
        .selectable(
          selected = isSelected,
          enabled = enabled,
          role = Role.RadioButton,
          onClick = { onAction(SelectTheme(id)) },
        )
        .padding(start = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = CenterVertically,
  ) {
    RadioButton(
      modifier = Modifier.padding(8.dp),
      enabled = enabled,
      selected = isSelected,
      onClick = null,
      colors = colors.radioButton(),
    )

    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = CenterVertically,
    ) {
      Text(
        modifier = Modifier.weight(1f, fill = false),
        text = name,
        style = typography.bodyLarge,
        fontWeight = if (isChosen) Medium else Normal,
        maxLines = 1,
        overflow = Ellipsis,
        color =
          when {
            !enabled -> colors.settingsCardText.disabled
            isChosen -> colors.settingsNavItemTextSelected
            else -> colors.settingsCardText
          },
      )

      if (isLocal) {
        Chip(text = Strings.settingsThemeLocalBadge, size = Small)
      }
    }

    SwatchDots(swatch = swatch, enabled = enabled)

    AktualIconButton(
      imageVector = AktualIcons.CheveronRight,
      contentDescription = Strings.settingsThemePreview(name),
      onClick = { onAction(InspectTheme(id)) },
    )
  }
}

@Composable
private fun SwatchDots(swatch: ThemeSwatch, enabled: Boolean) {
  val dots = remember(swatch) { listOf(swatch.background, swatch.surface, swatch.accent) }
  val ring = colors.settingsCardBackground
  Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
    dots.fastForEach { color ->
      Box(
        modifier =
          Modifier.size(18.dp)
            .background(color.disabledIf(!enabled), CircleShape)
            .border(2.dp, ring, CircleShape),
      )
    }
  }
}

@Preview
@Composable
private fun PreviewThemesPreference(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    ThemesPreference(
      localThemes =
        persistentListOf(
          LocalTheme(ThemeId("@local/a"), "Light (copy)", LightColors.swatch()),
          LocalTheme(ThemeId("@local/b"), "Midnight with stripes", MidnightColors.swatch()),
        ),
      selectedTheme = ThemeId("@local/b"),
      enabled = true,
      onAction = {},
    )
  }

@Preview
@Composable
private fun PreviewThemesPreferenceDisabled(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    ThemesPreference(
      localThemes = persistentListOf(),
      selectedTheme = LightColors.id,
      enabled = false,
      onAction = {},
    )
  }
