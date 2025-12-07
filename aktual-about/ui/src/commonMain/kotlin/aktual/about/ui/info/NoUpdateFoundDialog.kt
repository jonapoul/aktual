/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.about.ui.info

import aktual.core.model.ColorSchemeType
import aktual.core.ui.ColorSchemeParameters
import aktual.core.ui.DialogContent
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.l10n.Strings
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

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
internal fun NoUpdateFoundDialogContent(
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
private fun PreviewNoUpdatesContent(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  NoUpdateFoundDialogContent(
    onDismiss = {},
  )
}
