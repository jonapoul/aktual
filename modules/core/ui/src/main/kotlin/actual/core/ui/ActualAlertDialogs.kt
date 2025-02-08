package actual.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

@Composable
fun ActualAlertDialog(
  title: String,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  buttons: ComposableLambda? = null,
  theme: Theme = LocalTheme.current,
  properties: DialogProperties = DialogProperties(),
  content: @Composable () -> Unit,
) {
  BasicAlertDialog(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    properties = properties,
  ) {
    ActualAlertDialogContent(
      title = title,
      theme = theme,
      content = content,
      buttons = buttons,
    )
  }
}

@Composable
fun ActualAlertDialog(
  title: String,
  text: String,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  buttons: ComposableLambda? = null,
  theme: Theme = LocalTheme.current,
  properties: DialogProperties = DialogProperties(),
) {
  BasicAlertDialog(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    properties = properties,
  ) {
    ActualAlertDialogContent(
      title = title,
      theme = theme,
      buttons = buttons,
      content = { Text(text) },
    )
  }
}

@Composable
fun ActualAlertDialogContent(
  title: String,
  modifier: Modifier = Modifier,
  buttons: ComposableLambda? = null,
  theme: Theme = LocalTheme.current,
  tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
  content: @Composable () -> Unit,
) {
  Surface(
    modifier = modifier,
    shape = ActualDialogShape,
    color = theme.modalBackground,
    tonalElevation = tonalElevation,
  ) {
    Column {
      Text(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        text = title,
        fontFamily = ActualFontFamily,
        fontWeight = FontWeight.W700,
        fontSize = 25.sp,
        textAlign = TextAlign.Center,
      )

      val bottomPadding = if (buttons == null) 25.dp else 0.dp

      Column(
        modifier = Modifier
          .padding(end = 15.dp, start = 15.dp, bottom = bottomPadding)
          .fillMaxWidth(),
      ) {
        val contentStyle = LocalTextStyle.current.copy(fontSize = 14.sp, fontFamily = ActualFontFamily)
        CompositionLocalProvider(
          LocalContentColor provides theme.pageText,
          LocalTextStyle provides contentStyle,
        ) {
          content()
        }

        if (buttons != null) {
          //           Box(modifier = Modifier.align(Alignment.End)) {
          //             val textStyle = MaterialTheme.typography.fromToken(DialogTokens.ActionLabelTextFont)
          //             ProvideContentColorTextStyle(
          //               contentColor = colors.buttonPrimaryBackground,
          //               textStyle = textStyle,
          //               content = {
          //                 AlertDialogFlowRow(
          //                   mainAxisSpacing = ButtonsMainAxisSpacing,
          //                   crossAxisSpacing = ButtonsCrossAxisSpacing,
          //                 ) {
          //                   buttons()
          //                 }
          //               },
          //             )
          //           }
        }
      }
    }
  }
}

private val ActualDialogShape = RoundedCornerShape(size = 4.dp)
// private val ButtonsMainAxisSpacing = 8.dp
// private val ButtonsCrossAxisSpacing = 12.dp

@Preview
@Composable
private fun ExampleContentWithButtons() = PreviewActualColumn {
  val theme = LocalTheme.current
  ActualAlertDialogContent(
    title = "Hello world",
    buttons = {
      TextButton(onClick = { }) { Text("Delete", color = theme.errorText, fontFamily = ActualFontFamily) }
      TextButton(onClick = { }) { Text("Dismiss", fontFamily = ActualFontFamily) }
    },
    content = {
      Column(horizontalAlignment = Alignment.Start) {
        Text("This is some text with even more text here to show how it behaves when splitting over lines")
        PrimaryActualTextButton(text = "Click me", onClick = {})
        Text("This is some text")
        NormalActualTextButton(text = "Click me", onClick = {})
      }
    },
  )
}

@Preview
@Composable
private fun ExampleContentWithoutButtons() = PreviewActualColumn {
  ActualAlertDialogContent(
    title = "Hello world",
    buttons = null,
    content = {
      Column(horizontalAlignment = Alignment.Start) {
        Text("This is some text")
        PrimaryActualTextButton(text = "Click me", onClick = {})
        Text("This is some text")
        NormalActualTextButton(text = "Click me", onClick = {})
      }
    },
  )
}
