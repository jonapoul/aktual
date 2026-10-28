package aktual.about.ui.logs

import aktual.core.logging.LogLine
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.lineHeights
import aktual.core.ui.AktualTheme.textSizes
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
internal fun LogLineRow(
  line: LogLine,
  query: String,
  onTagClick: (String) -> Unit,
  onTagLongClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 3.dp),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Text(
      text = line.timestamp.time.timeString().take(TIME_LENGTH),
      color = colors.pageTextSubdued,
      fontFamily = Monospace,
      fontSize = LineFontSize,
      lineHeight = LineHeight,
    )

    LogLetterBadge(priority = line.priority, fontSize = LineFontSize)

    val highlight = colors.pillBackgroundSelected

    val tag = line.tag
    if (tag != null) {
      val isEllipsized = tag.length > MAX_TAG_LENGTH
      val tagText =
        remember(tag, query, highlight) {
          val shown = if (isEllipsized) tag.take(MAX_TAG_LENGTH) + "…" else tag
          buildAnnotatedString { appendHighlighted(shown, query, highlight) }
        }
      Text(
        modifier =
          Modifier.combinedClickable(
            onClick = { onTagClick(tag) },
            onLongClick = { onTagLongClick(tag) }.takeIf { isEllipsized },
          ),
        text = tagText,
        color = colors.pageTextPositive,
        fontFamily = Monospace,
        fontSize = LineFontSize,
        lineHeight = LineHeight,
      )
    }

    val message =
      remember(line.message, query, highlight) {
        buildAnnotatedString { appendHighlighted(line.message, query, highlight) }
      }

    SelectionContainer(modifier = Modifier.weight(1f)) {
      Text(
        text = message,
        color = line.priority.messageColor(colors),
        fontFamily = Monospace,
        fontSize = LineFontSize,
        lineHeight = LineHeight,
      )
    }
  }

internal fun AnnotatedString.Builder.appendHighlighted(
  text: String,
  query: String,
  highlight: Color,
) {
  if (query.isBlank()) {
    append(text)
    return
  }
  var start = 0
  var index = text.indexOf(query, ignoreCase = true)
  while (index >= 0) {
    append(text.substring(start, index))
    withStyle(SpanStyle(background = highlight)) {
      append(text.substring(index, index + query.length))
    }
    start = index + query.length
    index = text.indexOf(query, startIndex = start, ignoreCase = true)
  }
  append(text.substring(start))
}

private const val TIME_LENGTH = 8
private const val MAX_TAG_LENGTH = 15
private val LineFontSize = textSizes.small
private val LineHeight = lineHeights.large
