@file:Suppress("StringLiteralDuplication")

package aktual.core.ui

import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
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
fun AktualAlertDialog(
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
fun AktualAlertDialog(
  title: String?,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  buttons: (@Composable RowScope.() -> Unit)? = null,
  icon: ImageVector? = null,
  highlight: Color? = null,
  properties: DialogProperties = DialogProperties(),
  content: @Composable ColumnScope.() -> Unit,
) {
  AktualAlertDialog(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    properties = properties,
  ) {
    AktualAlertDialogContent(
      title = title,
      buttons = buttons,
      icon = icon,
      highlight = highlight,
      content = content,
    )
  }
}

@Composable
fun AktualAlertDialogContent(
  title: String?,
  buttons: (@Composable RowScope.() -> Unit)?,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  highlight: Color? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
    modifier = modifier.border(Hairline, highlight ?: colors.modalBorder, DialogShape),
    shape = DialogShape,
    color = colors.modalBackground,
    tonalElevation = AlertDialogDefaults.TonalElevation,
  ) {
    Column(
      modifier =
        Modifier.defaultMinSize(minWidth = 300.dp)
          .background(colors.modalBackground)
          .padding(Dimens.VeryLarge),
      horizontalAlignment = CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(Dimens.Medium, alignment = Alignment.Top),
    ) {
      Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        if (icon != null) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = highlight ?: colors.pageTextPositive,
          )
        }

        if (title != null) {
          Text(
            modifier = Modifier.padding(vertical = Dimens.Large),
            text = title,
            color = highlight ?: colors.pageTextPositive,
            style = typography.headlineSmall,
          )
        }
      }

      CompositionLocalProvider(LocalContentColor provides colors.pageText) {
        ProvideTextStyle(typography.bodyMedium) {
          content()
        }
      }

      if (buttons != null) {
        CompositionLocalProvider(LocalContentColor provides colors.pageTextPositive) {
          Row(modifier = Modifier.align(Alignment.End), content = buttons)
        }
      }
    }
  }
}

@Preview
@Composable
private fun PreviewExampleContentWithButtons(
  @PreviewParameter(ColoredParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    AktualAlertDialogContent(
      modifier = Modifier.padding(4.dp),
      title = "Hello world",
      buttons = {
        TextButton(onClick = {}) { Text("Delete", color = colors.errorText) }
        TextButton(onClick = {}) { Text("Dismiss") }
      },
      content = {
        Text(
          "This is some text with even more text here to show how it behaves when splitting over lines",
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
  @PreviewParameter(ColoredParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    AktualAlertDialogContent(
      modifier = Modifier.padding(4.dp),
      title = "Hello world",
      buttons = null,
      highlight = colors.noticeText,
      content = {
        Column(horizontalAlignment = CenterHorizontally) {
          Text("This is some text")
          PrimaryTextButton(text = "Click me", onClick = {})
          Text("This is some text")
          NormalTextButton(text = "Click me", onClick = {})
        }
      },
    )
  }
