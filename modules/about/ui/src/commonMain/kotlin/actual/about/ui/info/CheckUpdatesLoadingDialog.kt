package actual.about.ui.info

import actual.core.ui.DialogContent
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import actual.l10n.Strings
import alakazam.kotlin.compose.HorizontalSpacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
internal fun CheckUpdatesLoadingDialog(
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  BasicAlertDialog(
    modifier = modifier,
    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    onDismissRequest = onCancel,
    content = {
      CheckUpdatesLoadingDialogContent(
        onCancel = onCancel,
        theme = theme,
      )
    },
  )
}

@Composable
internal fun CheckUpdatesLoadingDialogContent(
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  DialogContent(
    modifier = modifier,
    theme = theme,
    title = null,
    content = {
      Row(
        modifier = Modifier.padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        CircularProgressIndicator(
          color = theme.pageTextPositive,
          trackColor = theme.dialogProgressWheelTrack,
        )

        HorizontalSpacer(15.dp)

        Text(
          text = Strings.infoCheckingUpdatesLoading,
          color = theme.pageText,
        )
      }
    },
    buttons = {
      TextButton(onClick = onCancel) {
        Text(
          text = Strings.infoCheckingUpdatesCancel,
          color = theme.pageTextPositive,
        )
      }
    },
  )
}
