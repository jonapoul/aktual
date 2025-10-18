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

import actual.about.ui.info.CheckUpdatesLoadingDialogContent
import actual.about.ui.info.NoUpdateFoundDialogContent
import actual.about.ui.info.UpdateCheckFailedDialogContent
import actual.about.ui.info.UpdateFoundDialogContent
import actual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewNoUpdatesContent() = PreviewThemedColumn {
  NoUpdateFoundDialogContent(
    onDismiss = {},
  )
}

@Preview
@Composable
private fun PreviewCheckUpdatesContent() = PreviewThemedColumn {
  CheckUpdatesLoadingDialogContent(
    onCancel = {},
  )
}

@Preview
@Composable
private fun PreviewCheckFailedContent() = PreviewThemedColumn {
  UpdateCheckFailedDialogContent(
    cause = "Something broke lol. And here's some other rubbish to show how the text looks when wrapping lines",
    onDismiss = {},
  )
}

@Preview
@Composable
private fun PreviewUpdateFoundContent() = PreviewThemedColumn {
  UpdateFoundDialogContent(
    currentVersion = "v1.2.3",
    latestVersion = "v2.3.4",
    latestUrl = "https://github.com/jonapoul/actual-android/releases/v2.3.4",
    onOpenUrl = {},
    onDismiss = {},
  )
}
