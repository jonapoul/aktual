@file:OptIn(ExperimentalMaterial3Api::class)

package actual.about.ui.info

import actual.core.ui.DialogContent
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import actual.l10n.Strings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun UpdateCheckFailedDialog(
  cause: String,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  BasicAlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    content = {
      UpdateCheckFailedDialogContent(
        cause = cause,
        onDismiss = onDismiss,
        theme = theme,
      )
    },
  )
}

@Composable
internal fun UpdateCheckFailedDialogContent(
  cause: String,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  DialogContent(
    modifier = modifier,
    theme = theme,
    title = Strings.infoCheckFailedTitle,
    icon = Icons.Filled.Warning,
    titleColor = theme.errorText,
    content = { Text(cause) },
    buttons = {
      TextButton(onClick = onDismiss) {
        Text(
          text = Strings.infoCheckFailedOk,
          color = theme.errorText,
        )
      }
    },
  )
}
