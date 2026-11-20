package aktual.prefs.ui.theme

import aktual.core.icons.AktualIcons
import aktual.core.icons.MoonStars
import aktual.core.icons.Sun
import aktual.core.l10n.Strings
import aktual.core.model.ThemeId
import aktual.core.theme.DarkColors
import aktual.core.theme.LightColors
import aktual.core.theme.MidnightColors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.disabledIf
import aktual.prefs.components.BasicPreferenceItem
import aktual.prefs.vm.theme.SystemThemePicker
import aktual.prefs.vm.theme.ThemeOption
import aktual.prefs.vm.theme.palette
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@Composable
internal fun SystemThemePreference(
  type: PickerType,
  picker: SystemThemePicker,
  customThemeNames: ImmutableMap<ThemeId, String>,
  onAction: OnAction<ThemeSettingsAction>,
  modifier: Modifier = Modifier,
) {
  val palette = picker.options.firstOrNull { it.id == picker.value }?.palette
  BasicPreferenceItem(
    modifier = modifier,
    title = type.title(),
    subtitle = null,
    icon = type.icon(),
    enabled = picker.enabled,
    onClick = { onAction(ShowPicker(type)) },
    rightContent = {
      Row(
        modifier = Modifier.padding(end = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = CenterVertically,
      ) {
        Text(
          text = themeName(picker.value, customThemeNames),
          style = typography.bodyMedium,
          color = colors.settingsCardText.disabledIf(!picker.enabled),
          maxLines = 1,
          overflow = Ellipsis,
        )
        if (palette != null) PaletteDots(palette, picker.enabled)
      }
    },
  )
}

@Composable
internal fun PickerType.title(): String =
  when (this) {
    LightTheme -> Strings.settingsLightTheme
    DarkTheme -> Strings.settingsDarkTheme
  }

private fun PickerType.icon(): ImageVector =
  when (this) {
    LightTheme -> AktualIcons.Sun
    DarkTheme -> AktualIcons.MoonStars
  }

// The first few palette colours, each tucked under the one before it
@Composable
internal fun PaletteDots(
  palette: ImmutableList<Color>,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  val ring = colors.settingsCardBackground
  Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(-DotOverlap)) {
    palette.take(DotCount).fastForEach { color ->
      Box(
        modifier =
          Modifier.size(DotSize)
            .background(color.disabledIf(!enabled), CircleShape)
            .border(2.dp, ring, CircleShape),
      )
    }
  }
}

private const val DotCount = 3
private val DotSize = 18.dp
private val DotOverlap = 6.dp

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
private fun PreviewSystemThemePreference(
  @PreviewParameter(SystemThemePreferenceProvider::class) params: ColorParams<Boolean>,
) =
  PreviewWithColorParams(params) {
    SystemThemePreference(
      type = DarkTheme,
      onAction = {},
      customThemeNames = persistentMapOf(PreviewCoffee to "Shades of Coffee"),
      picker =
        SystemThemePicker(
          value = PreviewCoffee,
          options =
            persistentListOf(
              ThemeOption(
                id = PreviewCoffee,
                name = "Shades of Coffee",
                palette = MidnightColors.palette(),
                isLocal = false,
                download = null,
              ),
            ),
          enabled = this,
        ),
    )
  }

private class SystemThemePreferenceProvider : ColorParamsProvider<Boolean>(true, false)

private val PreviewCoffee = ThemeId("user/coffee")
