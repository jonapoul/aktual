@file:OptIn(ExperimentalMaterial3Api::class)

package actual.core.ui

import actual.l10n.Dimens
import alakazam.android.ui.compose.HorizontalSpacer
import alakazam.android.ui.compose.VerticalSpacer
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
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
        .padding(Dimens.veryLarge),
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
            modifier = Modifier.padding(vertical = Dimens.large),
            text = title,
            color = titleColor,
          )
        }
      }

      VerticalSpacer(Dimens.medium)

      CompositionLocalProvider(LocalContentColor provides theme.pageText) {
        content()
      }

      VerticalSpacer(Dimens.medium)

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

@Preview
@Composable
private fun ExampleContentWithButtons() = PreviewColumn {
  val theme = LocalTheme.current
  DialogContent(
    title = "Hello world",
    buttons = {
      TextButton(onClick = { }) { Text("Delete", color = theme.errorText, fontFamily = ActualFontFamily) }
      TextButton(onClick = { }) { Text("Dismiss", fontFamily = ActualFontFamily) }
    },
    content = {
      Text("This is some text with even more text here to show how it behaves when splitting over lines")
      PrimaryTextButton(text = "Click me", onClick = {})
      Text("This is some text")
      NormalTextButton(text = "Click me", onClick = {})
    },
  )
}

@Preview
@Composable
private fun ExampleContentWithoutButtons() = PreviewColumn {
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
