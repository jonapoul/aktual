package aktual.about.ui.logs

import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.lineHeights
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.tabular
import aktual.logging.LogLine
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
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
  modifier: Modifier = Modifier,
) =
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 3.dp),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Text(
      text = line.timestamp.time.timeString().take(TIME_LENGTH),
      color = colors.pageTextSubdued,
      style = LocalTextStyle.current.tabular(),
      fontSize = LineFontSize,
      lineHeight = LineHeight,
    )

    LogLetterBadge(priority = line.priority, fontSize = LineFontSize)

    val highlight = colors.pillBackgroundSelected

    Column(modifier = Modifier.weight(1f)) {
      val tag = line.tag
      if (tag != null) {
        val tagText =
          remember(tag, query, highlight) {
            buildAnnotatedString { appendHighlighted(tag, query, highlight) }
          }
        Text(
          modifier = Modifier.fillMaxWidth().clickable { onTagClick(tag) },
          text = tagText,
          color = colors.accentText,
          fontSize = LineFontSize,
          lineHeight = LineHeight,
          overflow = Ellipsis,
          maxLines = 1,
        )
      }

      val message =
        remember(line.message, query, highlight) {
          buildAnnotatedString { appendHighlighted(line.message, query, highlight) }
        }

      SelectionContainer(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = message,
          color = line.priority.messageColor(colors),
          fontFamily = Monospace,
          fontSize = LineFontSize,
          lineHeight = LineHeight,
        )
      }
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
private val LineFontSize = textSizes.small
private val LineHeight = lineHeights.large
