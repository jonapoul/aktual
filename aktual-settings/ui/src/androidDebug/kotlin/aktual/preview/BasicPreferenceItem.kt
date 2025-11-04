/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.core.ui.PreviewThemedColumn
import aktual.settings.ui.BasicPreferenceItem
import aktual.settings.ui.Clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun WithIcon() = PreviewThemedColumn {
  BasicPreferenceItem(
    title = "Change the doodad",
    subtitle = "When you change this setting, the doodad will update. This might also affect the thingybob.",
    icon = Icons.Filled.Info,
    clickability = Clickable { },
  )
}

@Preview
@Composable
private fun WithoutIcon() = PreviewThemedColumn {
  BasicPreferenceItem(
    title = "Change the doodad",
    subtitle = "When you change this setting, the doodad will update. This might also affect the thingybob.",
    icon = null,
    clickability = Clickable { },
  )
}
