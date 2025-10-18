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
package actual.preview

import actual.core.ui.PreviewThemedColumn
import actual.settings.ui.BasicPreferenceItem
import actual.settings.ui.Clickable
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
