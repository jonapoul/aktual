@file:Suppress("StringLiteralDuplication")

package aktual.core.ui

import aktual.core.model.ColorSchemeType
import alakazam.compose.HorizontalSpacer
import alakazam.compose.VerticalSpacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
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
    DialogContent(title = title, theme = theme, content = content, buttons = buttons)
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
    DialogContent(title = title, theme = theme, buttons = buttons, content = { Text(text) })
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
      verticalArrangement = Arrangement.Top,
    ) {
      Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
          Icon(imageVector = icon, contentDescription = null, tint = titleColor)

          HorizontalSpacer(10.dp)
        }

        if (title != null) {
          Text(
            modifier = Modifier.padding(vertical = Dimens.Large),
            text = title,
            color = titleColor,
          )
        }
      }

      VerticalSpacer(Dimens.Medium)

      CompositionLocalProvider(LocalContentColor provides theme.pageText) { content() }

      VerticalSpacer(Dimens.Medium)

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
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType
) =
  PreviewWithColorScheme(type) {
    val theme = LocalTheme.current
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
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType
) =
  PreviewWithColorScheme(type) {
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
