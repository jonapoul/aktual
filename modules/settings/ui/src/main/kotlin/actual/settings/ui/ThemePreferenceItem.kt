package actual.settings.ui

import actual.core.colorscheme.ColorSchemeType
import actual.core.res.CoreStrings
import actual.core.ui.ButtonShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewWithColorScheme
import actual.core.ui.Theme
import actual.settings.res.SettingsStrings
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import dev.chrisbanes.haze.HazeState
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun ThemePreferenceItem(
  value: ColorSchemeType,
  onChange: (ColorSchemeType) -> Unit,
  modifier: Modifier = Modifier,
  hazeState: HazeState = remember { HazeState() },
) {
  BasicPreferenceItem(
    modifier = modifier,
    title = SettingsStrings.theme,
    subtitle = null,
    icon = value.icon(),
    clickability = NotClickable,
    hazeState = hazeState,
  ) {
    AppThemePicker(
      modifier = Modifier.padding(2.dp),
      selected = value,
      onSelect = onChange,
    )
  }
}

@Composable
private fun AppThemePicker(
  selected: ColorSchemeType,
  onSelect: (ColorSchemeType) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
  ) {
    TYPES.fastForEach { type ->
      TypeButton(
        modifier = Modifier.weight(1f),
        type = type,
        isSelected = selected == type,
        onClick = { onSelect(type) },
      )
    }
  }
}

private val TYPES = ColorSchemeType.entries.toImmutableList()

@Composable
private fun TypeButton(
  type: ColorSchemeType,
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
    modifier = modifier
      .padding(2.dp)
      .background(background, ButtonShape),
    onClick = onClick,
    enabled = true,
    colors = buttonColors,
  ) {
    Text(
      text = type.string(),
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.bodySmall,
      fontSize = 11.sp,
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
private fun ColorSchemeType.icon(): ImageVector = when (this) {
  ColorSchemeType.System -> Icons.Filled.Settings
  ColorSchemeType.Light -> Icons.Filled.LightMode
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
private fun PreviewDark() = PreviewThemed(ColorSchemeType.Dark)

@Preview
@Composable
private fun PreviewMidnight() = PreviewThemed(ColorSchemeType.Midnight)

@Composable
private fun PreviewThemed(
  initial: ColorSchemeType,
  modifier: Modifier = Modifier,
) {
  var selected by remember { mutableStateOf(initial) }
  PreviewWithColorScheme(
    schemeType = selected,
    modifier = modifier,
  ) {
    ThemePreferenceItem(
      value = selected,
      onChange = { newValue -> selected = newValue },
    )
  }
}
