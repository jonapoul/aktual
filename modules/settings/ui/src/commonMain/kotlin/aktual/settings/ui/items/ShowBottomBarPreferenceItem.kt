/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.settings.ui.items

import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import aktual.core.ui.switch
import aktual.l10n.Strings
import aktual.settings.ui.BasicPreferenceItem
import aktual.settings.ui.Clickable
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
    icon = icon(value),
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
private fun icon(isVisible: Boolean) = when (isVisible) {
  true -> Icons.Filled.Visibility
  false -> Icons.Filled.VisibilityOff
}
