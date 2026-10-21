package aktual.budget.navrail.ui

import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PreviewWithColors
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

// PostsOfflineNotification in packages/desktop-client/src/components/schedules
@Composable
internal fun UnpostedSchedulesDialog(
  onPost: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualAlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    content = { UnpostedSchedulesDialogContent(onPost, onDismiss) },
  )
}

@Composable
private fun UnpostedSchedulesDialogContent(
  onPost: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualAlertDialogContent(
    modifier = modifier,
    title = Strings.schedulesUnpostedTitle,
    content = {
      Column(verticalArrangement = spacedBy(8.dp)) {
        Text(text = Strings.schedulesUnpostedMessage)
        Text(text = Strings.schedulesUnpostedDevices)
      }
    },
    buttons = {
      TextButton(onClick = onDismiss) { Text(text = Strings.schedulesUnpostedLater) }
      TextButton(onClick = onPost) {
        Text(text = Strings.schedulesUnpostedPost, color = colors.numberPositive)
      }
    },
  )
}

@Preview
@Composable
private fun PreviewUnpostedSchedulesDialog(
  @PreviewParameter(ColoredParameters::class) colors: Colors,
) = PreviewWithColors(colors) { UnpostedSchedulesDialogContent(onPost = {}, onDismiss = {}) }
