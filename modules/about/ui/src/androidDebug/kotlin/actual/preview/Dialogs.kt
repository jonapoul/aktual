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
