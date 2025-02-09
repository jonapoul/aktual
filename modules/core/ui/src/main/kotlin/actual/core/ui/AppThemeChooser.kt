package actual.core.ui

import actual.core.colorscheme.ColorSchemeType
import actual.core.res.CoreStrings
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.toImmutableList

private val PADDING = 5.dp

@Composable
fun AppThemeChooser(
  selected: ColorSchemeType,
  onSelect: (ColorSchemeType) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
  ) {
    Text(
      modifier = Modifier.padding(PADDING),
      text = CoreStrings.themeChooser,
    )

    Row {
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

  IconButton(
    modifier = modifier
      .height(80.dp)
      .padding(PADDING)
      .background(background, ButtonShape),
    onClick = onClick,
    enabled = true,
    colors = buttonColors,
  ) {
    Column(
      modifier = Modifier.wrapContentHeight(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      val string = type.string()

      Icon(
        imageVector = type.icon(),
        contentDescription = string,
      )

      Text(
        text = string,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
      )
    }
  }
}

@Stable
@Composable
private fun Theme.colors(
  isPressed: Boolean,
  isSelected: Boolean,
) = IconButtonDefaults.filledIconButtonColors(
  containerColor = when {
    isPressed -> buttonNormalBackgroundHover
    isSelected -> buttonNormalSelectedBackground
    else -> buttonNormalBackground
  },
  contentColor = when {
    isPressed -> buttonNormalTextHover
    isSelected -> buttonNormalSelectedText
    else -> buttonNormalText
  },
  disabledContainerColor = buttonNormalDisabledBackground,
  disabledContentColor = buttonNormalDisabledText,
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
private fun PreviewSystem() = PreviewWithSchemeSelected(ColorSchemeType.System)

@Preview
@Composable
private fun PreviewLight() = PreviewWithSchemeSelected(ColorSchemeType.Light)

@Preview
@Composable
private fun PreviewDark() = PreviewWithSchemeSelected(ColorSchemeType.Dark)

@Preview
@Composable
private fun PreviewMidnight() = PreviewWithSchemeSelected(ColorSchemeType.Midnight)

@Composable
private fun PreviewWithSchemeSelected(
  initial: ColorSchemeType,
  modifier: Modifier = Modifier,
) {
  var selected by remember { mutableStateOf(initial) }
  ActualTheme(selected) {
    Surface(modifier = modifier) {
      AppThemeChooser(
        selected = selected,
        onSelect = { selected = it },
      )
    }
  }
}
