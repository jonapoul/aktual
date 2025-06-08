package actual.settings.ui.items

import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import actual.core.ui.switch
import actual.settings.res.Strings
import actual.settings.ui.BasicPreferenceItem
import actual.settings.ui.Clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState

@Composable
internal fun ShowBottomBarPreferenceItem(
  value: Boolean,
  onChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  hazeState: HazeState = remember { HazeState() },
  theme: Theme = LocalTheme.current,
) {
  BasicPreferenceItem(
    modifier = modifier.fillMaxWidth(),
    title = Strings.settingsShowBottomBar,
    subtitle = null,
    icon = regularIcon(value),
    clickability = Clickable { onChange(!value) },
    hazeState = hazeState,
    rightContent = {
      val interactionSource = remember { MutableInteractionSource() }
      val isPressed by interactionSource.collectIsPressedAsState()
      Switch(
        modifier = Modifier.padding(10.dp),
        checked = value,
        onCheckedChange = null,
        colors = theme.switch(isPressed),
      )
    },
  )
}

@Stable
private fun regularIcon(isVisible: Boolean) = when (isVisible) {
  true -> Icons.Filled.Visibility
  false -> Icons.Filled.VisibilityOff
}

@Preview
@Composable
private fun PreviewChecked() = PreviewColumn {
  ShowBottomBarPreferenceItem(
    value = true,
    onChange = {},
  )
}

@Preview
@Composable
private fun PreviewUnchecked() = PreviewColumn {
  ShowBottomBarPreferenceItem(
    value = false,
    onChange = {},
  )
}
