@file:Suppress("StringLiteralDuplication")

package aktual.core.ui

import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

/**
 * A wrapper around [BasicAlertDialog] that signals [DialogBlurState] so the background blur overlay
 * animates in while this dialog is showing.
 */
@Composable
fun AlertDialog(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  properties: DialogProperties = DialogProperties(),
  content: @Composable () -> Unit,
) {
  val dialogBlurState = LocalDialogBlurState.current
  DisposableEffect(Unit) {
    dialogBlurState.activeDialogCount++
    onDispose { dialogBlurState.activeDialogCount-- }
  }

  BasicAlertDialog(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    properties = properties,
    content = content,
  )
}

@Composable
fun AlertDialog(
  title: String?,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  buttons: (@Composable RowScope.() -> Unit)? = null,
  icon: ImageVector? = null,
  theme: Theme = LocalTheme.current,
  titleColor: Color = theme.pageTextPositive,
  properties: DialogProperties = DialogProperties(),
  content: @Composable ColumnScope.() -> Unit,
) {
  AlertDialog(onDismissRequest = onDismissRequest, modifier = modifier, properties = properties) {
    DialogContent(
      title = title,
      buttons = buttons,
      icon = icon,
      theme = theme,
      titleColor = titleColor,
      content = content,
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
      modifier =
        Modifier.defaultMinSize(minWidth = 300.dp)
          .background(theme.modalBackground)
          .padding(Dimens.VeryLarge),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(Dimens.Medium, alignment = Alignment.Top),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        if (icon != null) {
          Icon(imageVector = icon, contentDescription = null, tint = titleColor)
        }

        if (title != null) {
          Text(
            modifier = Modifier.padding(vertical = Dimens.Large),
            text = title,
            color = titleColor,
          )
        }
      }

      CompositionLocalProvider(LocalContentColor provides theme.pageText) { content() }

      if (buttons != null) {
        CompositionLocalProvider(LocalContentColor provides theme.pageTextPositive) {
          Row(modifier = Modifier.align(Alignment.End), content = buttons)
        }
      }
    }
  }
}

@Preview
@Composable
private fun PreviewExampleContentWithButtons(
  @PreviewParameter(ThemeParameters::class) theme: Theme
) =
  PreviewWithTheme(theme) {
    DialogContent(
      title = "Hello world",
      buttons = {
        TextButton(onClick = {}) { Text("Delete", color = theme.errorText) }
        TextButton(onClick = {}) { Text("Dismiss") }
      },
      content = {
        Text(
          "This is some text with even more text here to show how it behaves when splitting over lines"
        )
        PrimaryTextButton(text = "Click me", onClick = {})
        Text("This is some text")
        NormalTextButton(text = "Click me", onClick = {})
      },
    )
  }

@Preview
@Composable
private fun PreviewExampleContentWithoutButtons(
  @PreviewParameter(ThemeParameters::class) theme: Theme
) =
  PreviewWithTheme(theme) {
    DialogContent(
      title = "Hello world",
      buttons = null,
      content = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("This is some text")
          PrimaryTextButton(text = "Click me", onClick = {})
          Text("This is some text")
          NormalTextButton(text = "Click me", onClick = {})
        }
      },
    )
  }
