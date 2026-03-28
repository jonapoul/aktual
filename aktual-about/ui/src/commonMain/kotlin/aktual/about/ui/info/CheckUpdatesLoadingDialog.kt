package aktual.about.ui.info

import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AlertDialog
import aktual.core.ui.AnimatedLoading
import aktual.core.ui.DialogContent
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.ThemeParameters
import alakazam.compose.HorizontalSpacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
internal fun CheckUpdatesLoadingDialog(
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  AlertDialog(
    modifier = modifier,
    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    onDismissRequest = onCancel,
    content = { CheckUpdatesLoadingDialogContent(onCancel = onCancel, theme = theme) },
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
        AnimatedLoading()

        HorizontalSpacer(15.dp)

        Text(text = Strings.infoCheckingUpdatesLoading, color = theme.pageText)
      }
    },
    buttons = {
      TextButton(onClick = onCancel) {
        Text(text = Strings.infoCheckingUpdatesCancel, color = theme.pageTextPositive)
      }
    },
  )
}

@Preview
@Composable
private fun PreviewCheckUpdatesContent(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) { CheckUpdatesLoadingDialogContent(onCancel = {}) }
