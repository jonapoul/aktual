package actual.core.ui.previews

import actual.core.ui.DialogContent
import actual.core.ui.LocalTheme
import actual.core.ui.NormalTextButton
import actual.core.ui.PreviewThemedColumn
import actual.core.ui.PrimaryTextButton
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun ExampleContentWithButtons() = PreviewThemedColumn {
  val theme = LocalTheme.current
  DialogContent(
    title = "Hello world",
    buttons = {
      TextButton(onClick = { }) { Text("Delete", color = theme.errorText) }
      TextButton(onClick = { }) { Text("Dismiss") }
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
private fun ExampleContentWithoutButtons() = PreviewThemedColumn {
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
