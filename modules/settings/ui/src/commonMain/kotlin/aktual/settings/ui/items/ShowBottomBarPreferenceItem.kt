/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
