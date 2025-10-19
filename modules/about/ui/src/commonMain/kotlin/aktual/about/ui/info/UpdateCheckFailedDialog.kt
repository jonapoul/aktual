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
package aktual.about.ui.info

import aktual.core.ui.DialogContent
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import aktual.l10n.Strings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BasicAlertDialog
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
