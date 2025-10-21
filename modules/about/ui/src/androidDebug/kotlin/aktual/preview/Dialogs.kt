/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.about.ui.info.CheckUpdatesLoadingDialogContent
import aktual.about.ui.info.NoUpdateFoundDialogContent
import aktual.about.ui.info.UpdateCheckFailedDialogContent
import aktual.about.ui.info.UpdateFoundDialogContent
import aktual.core.ui.PreviewThemedColumn
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
    latestUrl = "https://github.com/jonapoul/aktual/releases/v2.3.4",
    onOpenUrl = {},
    onDismiss = {},
  )
}
