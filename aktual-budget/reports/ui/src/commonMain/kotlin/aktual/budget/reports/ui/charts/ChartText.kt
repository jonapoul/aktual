package aktual.budget.reports.ui.charts

import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.vm.TextData
import aktual.core.l10n.Strings
import aktual.core.ui.BareTextButton
import aktual.core.ui.CardShape
import aktual.core.ui.LocalTheme
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import org.intellij.lang.annotations.Language

@Composable
internal fun TextChart(
  data: TextData,
  compact: Boolean,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) =
  Column(modifier = modifier) {
    var isEditing by remember { mutableStateOf(false) }
    var editingContent by remember(data) { mutableStateOf(data.content) }
    val keyboard = LocalSoftwareKeyboardController.current

    val onSave = {
      keyboard?.hide()
      onAction(Action.SaveTextContent(data, editingContent))
      isEditing = false
    }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.weight(1f)) {
      if (isEditing) {
        BasicTextField(
          modifier = Modifier.fillMaxSize().padding(4.dp).verticalScrollWithBar(scrollState),
          value = editingContent,
          onValueChange = { editingContent = it },
          keyboardOptions =
            KeyboardOptions(
              autoCorrectEnabled = true,
              capitalization = KeyboardCapitalization.Sentences,
              keyboardType = KeyboardType.Text,
              imeAction = ImeAction.None,
            ),
        )
      } else {
        Markdown(
          modifier = Modifier.fillMaxSize().verticalScrollWithBar(scrollState),
          content = data.content,
          colors = markdownColor(theme.pageText),
        )
      }
    }

    // No editing in compact mode
    if (compact) return@Column

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      if (isEditing) {
        BareTextButton(
          modifier = Modifier.weight(1f),
          text = Strings.reportsTextDiscard,
          onClick = {
            editingContent = data.content
            isEditing = false
          },
        )

        NormalTextButton(
          modifier = Modifier.weight(1f),
          text = Strings.reportsTextSave,
          onClick = { onSave() },
        )
      } else {
        NormalTextButton(
          modifier = Modifier.fillMaxWidth(),
          text = Strings.reportsTextEdit,
          onClick = {
            editingContent = data.content
            isEditing = true
          },
        )
      }
    }
  }

@Preview
@Composable
private fun PreviewTextChart(
  @PreviewParameter(TextChartProvider::class) params: ThemedParams<TextChartParams>
) =
  PreviewWithColorScheme(schemeType = params.type, isPrivacyEnabled = params.data.private) {
    TextChart(
      modifier =
        Modifier.background(LocalTheme.current.tableBackground, CardShape)
          .width(WIDTH.dp)
          .let { m -> if (params.data.compact) m.height(300.dp) else m }
          .padding(5.dp),
      data = params.data.data,
      compact = params.data.compact,
      onAction = {},
    )
  }

private data class TextChartParams(val data: TextData, val compact: Boolean, val private: Boolean)

private class TextChartProvider :
  ThemedParameterProvider<TextChartParams>(
    listOf(PREVIEW_TEXT_DATA, PREVIEW_SHORT_TEXT_DATA).flatMap { data ->
      listOf(true, false).flatMap { compact ->
        listOf(true, false).map { private -> TextChartParams(data, compact, private) }
      }
    }
  )

@Language("Markdown")
private val MARKDOWN_1 =
  """
  # Title
  ## Subtitle
  ### Sub-subtitle
  Text goes here
  - Bullet 1
  - Bullet 2

  More text goes below - *lorem ipsum* blah blah who cares just trying to **split over multiple lines like this**.

  ![Image](https://dummyimage.com/600x400/000/fff)

  Here's a [link to Google](https://www.google.com)

  ```
  import androidx.compose.foundation.background
  import androidx.compose.foundation.gestures.scrollable
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.rememberScrollState
  import androidx.compose.foundation.text.BasicTextField
  import androidx.compose.foundation.verticalScroll
  import androidx.compose.material.MaterialTheme
  import androidx.compose.material.Surface
  import androidx.compose.material.Text
  import androidx.compose.runtime.*
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.graphics.SolidColor
  import androidx.compose.ui.text.TextStyle
  import androidx.compose.ui.unit.dp
  import androidx.compose.ui.tooling.preview.Preview

  @Composable
  fun ScrollableBasicTextField() {
      var text by remember { mutableStateOf("") }
      val scrollState = rememberScrollState()

      Box(
          modifier = Modifier
              .height(150.dp) // fixed height container
              .fillMaxWidth()
              .background(MaterialTheme.colors.surface)
              .verticalScroll(scrollState) // enables vertical scrolling
              .padding(8.dp)
      ) {
          BasicTextField(
              value = text,
              onValueChange = { text = it },
              modifier = Modifier
                  .fillMaxWidth(),
              textStyle = TextStyle.Default.copy(color = MaterialTheme.colors.onSurface),
              cursorBrush = SolidColor(MaterialTheme.colors.primary),
              maxLines = Int.MAX_VALUE, // allow unlimited lines
          )
      }
  }

  @Preview(showBackground = true)
  @Composable
  fun PreviewScrollableBasicTextField() {
      MaterialTheme {
          Surface {
              ScrollableBasicTextField()
          }
      }
  }
  ```

  ## Explanation:

  - Box sets fixed height & fills width.
  - verticalScroll(scrollState) makes the whole Box scrollable vertically.
  - BasicTextField inside takes all available width and unlimited lines (maxLines = Int.MAX_VALUE).
  - When the text is long enough to overflow the height, vertical scroll kicks in.
  - You can add a placeholder, decorationBox, or other modifiers if you want.

  ## Bonus: Scroll to cursor as you type (advanced)

  If you want the text field to auto-scroll as the user types beyond the visible area, that requires tracking cursor position and syncing scroll. It’s a bit more complex but doable. Want me to prepare that snippet?

  Does this solve your scrollable BasicTextField need? Want me to add placeholder support or styling next?
  """
    .trimIndent()

@Language("Markdown")
private val MARKDOWN_SHORT =
  """
  - Bullet 1
  - Bullet 2

  More text goes below - *lorem ipsum* blah blah who cares just trying to **split over multiple lines like this**. Here's a [link to Google](https://www.google.com)
  """
    .trimIndent()

internal val PREVIEW_TEXT_DATA = TextData(content = MARKDOWN_1)

internal val PREVIEW_SHORT_TEXT_DATA = TextData(content = MARKDOWN_SHORT)
