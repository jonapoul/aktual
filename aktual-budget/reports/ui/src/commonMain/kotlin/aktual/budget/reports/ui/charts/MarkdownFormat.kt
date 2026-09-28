package aktual.budget.reports.ui.charts

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange

internal enum class MarkdownFormat {
  Bold,
  Italic,
  Heading,
  Bullet,
  Link,
}

internal data class FormattedText(val text: String, val selection: TextRange)

internal fun TextFieldState.applyFormat(format: MarkdownFormat) {
  val formatted = format(text.toString(), selection, format)
  edit {
    replace(0, length, formatted.text)
    selection = formatted.selection
  }
}

internal fun format(text: String, selection: TextRange, format: MarkdownFormat): FormattedText =
  when (format) {
    Bold -> wrap(text, selection, "**", "**")
    Italic -> wrap(text, selection, "_", "_")
    Heading -> toggleLinePrefix(text, selection, "## ")
    Bullet -> toggleLinePrefix(text, selection, "- ")
    Link -> link(text, selection)
  }

private fun wrap(text: String, selection: TextRange, before: String, after: String): FormattedText {
  val selected = text.substring(selection.min, selection.max)
  val newText = text.replaceRange(selection.min, selection.max, before + selected + after)
  val start = selection.min + before.length
  return FormattedText(newText, TextRange(start, start + selected.length))
}

// Removes the prefix if the selection's line already has it, otherwise adds it
private fun toggleLinePrefix(text: String, selection: TextRange, prefix: String): FormattedText {
  val lineStart = text.lastIndexOf('\n', startIndex = selection.min - 1) + 1
  return if (text.startsWith(prefix, lineStart)) {
    val newText = text.removeRange(lineStart, lineStart + prefix.length)
    FormattedText(newText, selection.shift(-prefix.length, floor = lineStart))
  } else {
    val newText = text.replaceRange(lineStart, lineStart, prefix)
    FormattedText(newText, selection.shift(prefix.length, floor = lineStart))
  }
}

private fun TextRange.shift(by: Int, floor: Int) =
  TextRange(maxOf(floor, start + by), maxOf(floor, end + by))

// Selected text becomes the label, with the cursor left where the user needs to type next
private fun link(text: String, selection: TextRange): FormattedText {
  val selected = text.substring(selection.min, selection.max)
  val newText = text.replaceRange(selection.min, selection.max, "[$selected]()")
  val cursor =
    if (selected.isEmpty()) {
      selection.min + 1
    } else {
      selection.min + selected.length + 3
    }
  return FormattedText(newText, TextRange(cursor))
}
