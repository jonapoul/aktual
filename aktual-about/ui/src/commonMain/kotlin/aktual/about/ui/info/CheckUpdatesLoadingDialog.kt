package aktual.about.ui.info

import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AnimatedLoading
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PreviewWithColors
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
) {
  AktualAlertDialog(
    modifier = modifier,
    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    onDismissRequest = onCancel,
    content = { CheckUpdatesLoadingDialogContent(onCancel = onCancel) },
  )
}

@Composable
internal fun CheckUpdatesLoadingDialogContent(
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualAlertDialogContent(
    modifier = modifier,
    title = null,
    content = {
      Row(
        modifier = Modifier.padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        AnimatedLoading()

        HorizontalSpacer(15.dp)

        Text(text = Strings.infoCheckingUpdatesLoading, color = colors.pageText)
      }
    },
    buttons = {
      TextButton(onClick = onCancel) {
        Text(text = Strings.infoCheckingUpdatesCancel, color = colors.pageTextPositive)
      }
    },
  )
}

@Preview
@Composable
private fun PreviewCheckUpdatesContent(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { CheckUpdatesLoadingDialogContent(onCancel = {}) }
