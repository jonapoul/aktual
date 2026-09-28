package aktual.budget.reports.ui.charts

import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.vm.TextAlign
import aktual.budget.reports.vm.TextData
import aktual.core.icons.material.Check
import aktual.core.icons.material.Edit
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.PrimaryTextButton
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign as ComposeTextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownTypography
import org.intellij.lang.annotations.Language

@Composable
internal fun TextChart(
  data: TextData,
  compact: Boolean,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    var isEditing by rememberSaveable { mutableStateOf(false) }
    val editState =
      rememberSaveable(data, saver = TextFieldState.Saver) { TextFieldState(data.content) }
    var editAlign by rememberSaveable(data) { mutableStateOf(data.align) }
    var editorMode by rememberSaveable { mutableStateOf(EditorMode.Write) }
    val keyboard = LocalSoftwareKeyboardController.current
    val hasChanges =
      isEditing && (editState.text.toString() != data.content || editAlign != data.align)

    if (!compact) {
      SideEffect(hasChanges) { onAction(Action.SetUnsavedText(hasChanges)) }
    }

    if (isEditing) {
      TextEditorToolbar(
        mode = editorMode,
        onMode = { editorMode = it },
        align = editAlign,
        onAlign = { editAlign = it },
        onFormat = { editState.applyFormat(it) },
      )
    }

    Box(modifier = Modifier.weight(1f)) {
      when {
        isEditing && editorMode == Rendered -> RenderedEditorText(editState, editAlign)
        isEditing -> TextEditor(editState)
        data.content.isBlank() -> EmptyText(compact)
        compact -> CompactMarkdown(data)
        else -> FullMarkdown(data)
      }
    }

    // No editing in compact mode
    if (compact) return

    TextChartButtons(
      isEditing = isEditing,
      hasChanges = hasChanges,
      onEdit = {
        editState.setTextAndPlaceCursorAtEnd(data.content)
        editAlign = data.align
        editorMode = Write
        isEditing = true
      },
      onCancel = {
        keyboard?.hide()
        editState.setTextAndPlaceCursorAtEnd(data.content)
        editAlign = data.align
        isEditing = false
      },
      onSave = {
        keyboard?.hide()
        onAction(Action.SaveText(editState.text.toString(), editAlign))
        isEditing = false
      },
    )
  }
}

@Composable
private fun TextEditor(state: TextFieldState, modifier: Modifier = Modifier) =
  AktualTextField(
    modifier = modifier.fillMaxSize(),
    state = state,
    placeholderText = Strings.reportsTextPlaceholder,
    textStyle = LocalTextStyle.current.copy(fontFamily = Monospace),
    keyboardOptions =
      KeyboardOptions(
        autoCorrectEnabled = true,
        capitalization = Sentences,
        keyboardType = KeyboardType.Text,
        imeAction = None,
      ),
  )

@Composable
private fun RenderedEditorText(state: TextFieldState, align: TextAlign) {
  val content = state.text.toString()
  if (content.isBlank()) EmptyText(compact = false) else FullMarkdown(TextData(content, align))
}

@Composable
private fun EmptyText(compact: Boolean, modifier: Modifier = Modifier) =
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Center) {
    Text(
      text = if (compact) Strings.reportsTextEmptyCompact else Strings.reportsTextEmpty,
      color = colors.tableTextSubdued,
      textAlign = Center,
    )
  }

@Composable
private fun CompactMarkdown(data: TextData, modifier: Modifier = Modifier) =
  Box(modifier = modifier.fillMaxSize()) {
    // Not scrollable, but the state tells us when the text overflows
    val scrollState = rememberScrollState()
    Markdown(
      modifier = Modifier.fillMaxWidth().verticalScroll(scrollState, enabled = false),
      content = data.content,
      colors = textChartMarkdownColors(),
      typography = textChartMarkdownTypography(data.align),
      components = alignedMarkdownComponents(data.align),
    )

    if (scrollState.canScrollForward) {
      Box(
        modifier =
          Modifier.align(BottomCenter)
            .fillMaxWidth()
            .height(FadeHeight)
            .background(Brush.verticalGradient(listOf(Transparent, colors.tableBackground)))
      )
    }
  }

@Composable
private fun FullMarkdown(data: TextData, modifier: Modifier = Modifier) =
  Markdown(
    modifier = modifier.fillMaxSize().verticalScrollWithBar(),
    content = data.content,
    colors = textChartMarkdownColors(),
    typography = textChartMarkdownTypography(data.align),
    components = alignedMarkdownComponents(data.align),
  )

@Composable
private fun TextChartButtons(
  isEditing: Boolean,
  hasChanges: Boolean,
  onEdit: () -> Unit,
  onCancel: () -> Unit,
  onSave: () -> Unit,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    if (isEditing) {
      if (hasChanges) {
        PrimaryTextButton(
          modifier = Modifier.weight(1f),
          text = Strings.reportsTextDiscard,
          colors = { pressed -> colors.errorPrimary(pressed) },
          onClick = onCancel,
        )
      } else {
        NormalTextButton(
          modifier = Modifier.weight(1f),
          text = Strings.reportsTextCancel,
          onClick = onCancel,
        )
      }

      PrimaryTextButton(
        modifier = Modifier.weight(1f),
        text = Strings.reportsTextSave,
        isEnabled = hasChanges,
        prefix = { Icon(imageVector = MaterialIcons.Check, contentDescription = null) },
        onClick = onSave,
      )
    } else {
      PrimaryTextButton(
        modifier = Modifier.fillMaxWidth(),
        text = Strings.reportsTextEdit,
        prefix = { Icon(imageVector = MaterialIcons.Edit, contentDescription = null) },
        onClick = onEdit,
      )
    }
  }

private val FadeHeight = 40.dp

@Composable
private fun Colors.errorPrimary(isPressed: Boolean) =
  ButtonDefaults.buttonColors(
    containerColor = if (isPressed) errorBorder else errorBackground,
    contentColor = if (isPressed) errorBackground else errorText,
  )

@Composable
private fun textChartMarkdownColors() =
  markdownColor(
    text = colors.tableText,
    codeBackground = colors.tableRowBackgroundHover,
    dividerColor = colors.tableBorder,
    tableBackground = colors.tableBackground,
  )

@Composable
private fun textChartMarkdownTypography(align: TextAlign): MarkdownTypography {
  val composeAlign =
    when (align) {
      Center -> ComposeTextAlign.Center
      Right -> ComposeTextAlign.Right
      Left,
      Unknown -> ComposeTextAlign.Left
    }
  fun TextStyle.aligned() = copy(textAlign = composeAlign)
  val type = MaterialTheme.typography
  return markdownTypography(
    h1 = type.displayLarge.aligned(),
    h2 = type.displayMedium.aligned(),
    h3 = type.displaySmall.aligned(),
    h4 = type.headlineMedium.aligned(),
    h5 = type.headlineSmall.aligned(),
    h6 = type.titleLarge.aligned(),
    text = type.bodyLarge.aligned(),
    quote = type.bodyMedium.plus(SpanStyle(fontStyle = FontStyle.Italic)).aligned(),
    paragraph = type.bodyLarge.aligned(),
    ordered = type.bodyLarge.aligned(),
    bullet = type.bodyLarge.aligned(),
    list = type.bodyLarge.aligned(),
    textLink =
      TextLinkStyles(style = SpanStyle(color = colors.pageTextLink, textDecoration = Underline)),
  )
}

@Preview
@Composable
private fun PreviewTextChart(
  @PreviewParameter(TextChartProvider::class) params: ColoredParams<TextChartParams>
) =
  PreviewWithColors(params.colors, isPrivacyEnabled = params.data.private) {
    TextChart(
      modifier =
        Modifier.background(colors.tableBackground, CardShape)
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
  ColoredParameterProvider<TextChartParams>(
    listOf(PREVIEW_TEXT_DATA, PREVIEW_SHORT_TEXT_DATA, TextData(content = "")).flatMap { data ->
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
      import androidx.compose.foundation.text.input.TextFieldState
      import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
      import androidx.compose.foundation.verticalScroll
      import androidx.compose.material.MaterialTheme
      import androidx.compose.material.Surface
      import androidx.compose.material.Text
      import androidx.compose.runtime.*
      import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Brush
      import androidx.compose.ui.graphics.SolidColor
      import androidx.compose.ui.text.TextStyle
      import androidx.compose.ui.unit.dp
      import androidx.compose.ui.text.SpanStyle
      import androidx.compose.ui.text.TextLinkStyles
  import androidx.compose.ui.text.TextStyle
  import androidx.compose.ui.text.font.FontStyle
  import androidx.compose.ui.text.style.TextAlign as ComposeTextAlign
      import androidx.compose.ui.text.font.FontFamily
      import androidx.compose.ui.text.style.TextDecoration
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
