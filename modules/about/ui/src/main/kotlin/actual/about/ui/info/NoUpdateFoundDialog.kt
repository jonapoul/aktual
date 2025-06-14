package actual.about.ui.info

import actual.about.res.Strings
import actual.core.ui.DialogContent
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun NoUpdateFoundDialog(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  BasicAlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    content = {
      NoUpdateFoundDialogContent(
        onDismiss = onDismiss,
        theme = theme,
      )
    },
  )
}

@Composable
private fun NoUpdateFoundDialogContent(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  DialogContent(
    modifier = modifier,
    theme = theme,
    title = Strings.infoNoUpdateTitle,
    content = {
      Text(text = Strings.infoNoUpdateMessage)
    },
    buttons = {
      TextButton(onClick = onDismiss) {
        Text(
          text = Strings.infoNoUpdateOk,
          color = theme.pageTextPositive,
        )
      }
    },
  )
}

@Preview
@Composable
private fun PreviewContent() = PreviewColumn {
  NoUpdateFoundDialogContent(
    onDismiss = {},
  )
}
