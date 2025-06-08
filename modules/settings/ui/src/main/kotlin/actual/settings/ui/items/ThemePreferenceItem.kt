package actual.settings.ui.items

import actual.core.model.ColorSchemeType
import actual.core.model.DarkColorSchemeType
import actual.core.res.CoreStrings
import actual.core.ui.ActualFontFamily
import actual.core.ui.AlertDialog
import actual.core.ui.ButtonShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewWithColorScheme
import actual.core.ui.Theme
import actual.settings.res.Strings
import actual.settings.ui.BasicPreferenceItem
import actual.settings.ui.NotClickable
import actual.settings.vm.ThemeConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import dev.chrisbanes.haze.HazeState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ThemePreferenceItem(
  config: ThemeConfig,
  onChange: (ThemeConfig) -> Unit,
  modifier: Modifier = Modifier,
  hazeState: HazeState = remember { HazeState() },
) {
  var openRegularDialog by remember { mutableStateOf(false) }
  var openDarkDialog by remember { mutableStateOf(false) }
  val (theme, darkTheme) = config

  Column(
    modifier = modifier.fillMaxWidth(),
  ) {
    BasicPreferenceItem(
      title = Strings.settingsTheme,
      subtitle = null,
      icon = theme.regularIcon(),
      clickability = NotClickable,
      hazeState = hazeState,
    ) {
      TypeButton(
        modifier = Modifier
          .fillMaxWidth()
          .padding(3.dp),
        type = theme,
        isSelected = false,
        onClick = { openRegularDialog = !openRegularDialog },
      )
    }

    if (config.theme == ColorSchemeType.System) {
      BasicPreferenceItem(
        title = Strings.settingsDarkTheme,
        subtitle = null,
        icon = darkTheme.darkIcon(),
        clickability = NotClickable,
        hazeState = hazeState,
      ) {
        TypeButton(
          modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp),
          type = darkTheme,
          isSelected = false,
          onClick = { openDarkDialog = !openDarkDialog },
        )
      }
    }
  }

  if (openRegularDialog) {
    ThemeChooserDialog(
      selected = theme,
      options = REGULAR_OPTIONS,
      onDismissRequest = { openRegularDialog = false },
      onSelect = { newValue ->
        openRegularDialog = false
        onChange(config.copy(theme = newValue))
      },
    )
  }

  if (openDarkDialog) {
    ThemeChooserDialog(
      selected = darkTheme,
      options = DARK_OPTIONS,
      onDismissRequest = { openDarkDialog = false },
      onSelect = { newValue ->
        openDarkDialog = false
        onChange(config.copy(darkTheme = newValue))
      },
    )
  }
}

@Composable
private fun <T : ColorSchemeType> ThemeChooserDialog(
  selected: T,
  options: ImmutableList<T>,
  onSelect: (T) -> Unit,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
) = AlertDialog(
  modifier = modifier,
  title = Strings.settingsTheme,
  onDismissRequest = onDismissRequest,
  buttons = {
    TextButton(onClick = onDismissRequest) {
      Text(
        text = Strings.settingsDialogDismiss,
        fontFamily = ActualFontFamily,
      )
    }
  },
  content = {
    ThemeChooserDialogContent(
      selected = selected,
      options = options,
      onSelect = onSelect,
    )
  },
)

private val REGULAR_OPTIONS = persistentListOf<ColorSchemeType>(
  ColorSchemeType.System,
  ColorSchemeType.Light,
  ColorSchemeType.Dark,
  ColorSchemeType.Midnight,
)

private val DARK_OPTIONS = persistentListOf<DarkColorSchemeType>(
  ColorSchemeType.Dark,
  ColorSchemeType.Midnight,
)

@Composable
private fun <T : ColorSchemeType> ColumnScope.ThemeChooserDialogContent(
  selected: T,
  options: ImmutableList<T>,
  onSelect: (T) -> Unit,
) {
  options.fastForEach { type ->
    TypeButton(
      modifier = Modifier
        .fillMaxWidth()
        .padding(1.dp),
      type = type,
      isSelected = selected == type,
      onClick = { onSelect(type) },
    )
  }
}

@Composable
private fun <T : ColorSchemeType> TypeButton(
  type: T,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val buttonColors = theme.colors(isPressed, isSelected)
  val background = buttonColors.containerColor

  TextButton(
    modifier = modifier.background(background, ButtonShape),
    onClick = onClick,
    enabled = true,
    colors = buttonColors,
  ) {
    Text(
      text = type.string(),
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.bodySmall,
    )
  }
}

@Stable
@Composable
private fun Theme.colors(
  isPressed: Boolean,
  isSelected: Boolean,
) = ButtonDefaults.textButtonColors(
  containerColor = when {
    isPressed -> buttonPrimaryBackgroundHover
    isSelected -> buttonPrimaryBackground
    else -> buttonNormalBackground
  },
  contentColor = when {
    isPressed -> buttonPrimaryTextHover
    isSelected -> buttonPrimaryText
    else -> buttonNormalText
  },
  disabledContainerColor = buttonPrimaryDisabledBackground,
  disabledContentColor = buttonPrimaryDisabledText,
)

@Composable
@ReadOnlyComposable
private fun ColorSchemeType.string(): String = when (this) {
  ColorSchemeType.System -> CoreStrings.themeSystem
  ColorSchemeType.Light -> CoreStrings.themeLight
  ColorSchemeType.Dark -> CoreStrings.themeDark
  ColorSchemeType.Midnight -> CoreStrings.themeMidnight
}

@Composable
@ReadOnlyComposable
private fun ColorSchemeType.regularIcon(): ImageVector = when (this) {
  ColorSchemeType.System -> Icons.Filled.Settings
  ColorSchemeType.Light -> Icons.Filled.LightMode
  ColorSchemeType.Dark -> Icons.Filled.WbTwilight
  ColorSchemeType.Midnight -> Icons.Filled.DarkMode
}

@Composable
@ReadOnlyComposable
private fun DarkColorSchemeType.darkIcon(): ImageVector = when (this) {
  ColorSchemeType.Dark -> Icons.Filled.WbTwilight
  ColorSchemeType.Midnight -> Icons.Filled.DarkMode
}

@Preview
@Composable
private fun PreviewSystem() = PreviewThemed(ColorSchemeType.System)

@Preview
@Composable
private fun PreviewLight() = PreviewThemed(ColorSchemeType.Light)

@Preview
@Composable
private fun PreviewDark() = PreviewThemed(ColorSchemeType.Dark, dark = ColorSchemeType.Dark)

@Preview
@Composable
private fun PreviewMidnight() = PreviewThemed(ColorSchemeType.Dark, dark = ColorSchemeType.Midnight)

@Composable
private fun PreviewThemed(
  theme: ColorSchemeType,
  modifier: Modifier = Modifier,
  dark: DarkColorSchemeType = ColorSchemeType.Dark,
) {
  var config by remember { mutableStateOf(ThemeConfig(theme, dark)) }
  val schemeType = when (config.theme) {
    ColorSchemeType.System -> ColorSchemeType.System
    ColorSchemeType.Light -> ColorSchemeType.Light
    ColorSchemeType.Midnight -> ColorSchemeType.Midnight
    ColorSchemeType.Dark -> config.darkTheme
  }
  PreviewWithColorScheme(
    schemeType = schemeType,
    modifier = modifier,
  ) {
    ThemePreferenceItem(
      config = ThemeConfig(schemeType, ColorSchemeType.Midnight),
      onChange = { newValue -> config = newValue },
    )
  }
}
