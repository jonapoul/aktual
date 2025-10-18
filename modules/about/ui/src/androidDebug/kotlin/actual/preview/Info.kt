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

import actual.about.ui.info.BuildStateItem
import actual.about.ui.info.InfoBuildState
import actual.about.ui.info.InfoButtons
import actual.about.ui.info.InfoHeader
import actual.about.ui.info.InfoScaffold
import actual.core.ui.PreviewThemedColumn
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
private fun PreviewInfoButtons() = PreviewThemedColumn {
  InfoButtons(
    modifier = Modifier.width(300.dp),
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewHeader() = PreviewThemedColumn {
  InfoHeader(year = 2025)
}

@Preview
@Composable
private fun PreviewRegularItem() = PreviewThemedColumn {
  BuildStateItem(
    icon = Icons.Filled.Info,
    title = "Info",
    subtitle = "More info",
    onClick = null,
  )
}

@Preview
@Composable
private fun PreviewClickableItem() = PreviewThemedColumn {
  BuildStateItem(
    icon = Icons.Filled.Numbers,
    title = "Info",
    subtitle = "More info",
    onClick = {},
  )
}

@TripleScreenPreview
@Composable
private fun PreviewInfo() = PreviewThemedScreen {
  InfoScaffold(
    buildState = PreviewBuildState,
    onAction = {},
  )
}

@TripleScreenPreview
@Composable
private fun PreviewBuildState() = PreviewThemedScreen {
  InfoBuildState(
    modifier = Modifier.fillMaxWidth(),
    buildState = PreviewBuildState,
  )
}
