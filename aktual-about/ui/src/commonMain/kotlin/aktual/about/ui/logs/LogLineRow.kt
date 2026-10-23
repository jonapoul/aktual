package aktual.about.ui.logs

import aktual.core.logging.LogLine
import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun LogLineRow(
  line: LogLine,
  query: String,
  onTagClick: (String) -> Unit,
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

    val tag = line.tag
    val tagColor = colors.pageTextPositive
    val messageColor = line.priority.messageColor(colors)
    val highlight = colors.pillBackgroundSelected
    val currentOnTagClick by rememberUpdatedState(onTagClick)
    val text =
      remember(line, query, tagColor, messageColor, highlight) {
        buildAnnotatedString {
          if (tag != null) {
            val link = LinkAnnotation.Clickable(tag) { currentOnTagClick(tag) }
            withLink(link) {
              withStyle(SpanStyle(color = tagColor)) { appendHighlighted(tag, query, highlight) }
            }
            append(' ')
          }
          withStyle(SpanStyle(color = messageColor)) {
            appendHighlighted(line.message, query, highlight)
          }
        }
      }

    SelectionContainer(modifier = Modifier.weight(1f)) {
      Text(
        text = text,
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
private val LineFontSize = 11.5.sp
private val LineHeight = 16.sp
