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
package aktual.core.ui

import alakazam.kotlin.compose.HorizontalSpacer
import alakazam.kotlin.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun AlertDialog(
  title: String,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  buttons: (@Composable RowScope.() -> Unit)? = null,
  theme: Theme = LocalTheme.current,
  properties: DialogProperties = DialogProperties(),
  content: @Composable ColumnScope.() -> Unit,
) {
  BasicAlertDialog(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    properties = properties,
  ) {
    DialogContent(
      title = title,
      theme = theme,
      content = content,
      buttons = buttons,
    )
  }
}

@Composable
fun AlertDialog(
  title: String,
  text: String,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  buttons: (@Composable RowScope.() -> Unit)? = null,
  theme: Theme = LocalTheme.current,
  properties: DialogProperties = DialogProperties(),
) {
  BasicAlertDialog(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    properties = properties,
  ) {
    DialogContent(
      title = title,
      theme = theme,
      buttons = buttons,
      content = { Text(text) },
    )
  }
}

@Composable
fun DialogContent(
  title: String?,
  buttons: (@Composable RowScope.() -> Unit)?,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  theme: Theme = LocalTheme.current,
  titleColor: Color = theme.pageTextPositive,
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
    modifier = modifier,
    shape = DialogShape,
    color = theme.modalBackground,
    tonalElevation = AlertDialogDefaults.TonalElevation,
  ) {
    Column(
      modifier = Modifier
        .defaultMinSize(minWidth = 300.dp)
        .background(theme.dialogBackground)
        .padding(Dimens.VeryLarge),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top,
    ) {
      Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        icon?.let {
          Icon(
            imageVector = it,
            contentDescription = null,
            tint = titleColor,
          )

          HorizontalSpacer(10.dp)
        }

        title?.let {
          Text(
            modifier = Modifier.padding(vertical = Dimens.Large),
            text = title,
            color = titleColor,
          )
        }
      }

      VerticalSpacer(Dimens.Medium)

      CompositionLocalProvider(LocalContentColor provides theme.pageText) {
        content()
      }

      VerticalSpacer(Dimens.Medium)

      buttons?.let {
        CompositionLocalProvider(LocalContentColor provides theme.pageTextPositive) {
          Row(
            modifier = Modifier.align(Alignment.End),
            content = buttons,
          )
        }
      }
    }
  }
}
