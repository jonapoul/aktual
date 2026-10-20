package aktual.budget.transactions.ui.edit

import aktual.budget.model.TagId
import aktual.budget.model.tagRangesInNotes
import aktual.budget.tags.domain.TagLabel
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.contrastingTextColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable private data class Chip(val start: Int, val end: Int, val background: Color)

@Immutable private data class TaggedText(val text: AnnotatedString, val chips: List<Chip>)

// As upstream's NotesTagFormatter, each #tag shows as a chip in its tag's color
@Composable
internal fun TaggedNotes(
  notes: String,
  tags: ImmutableList<TagLabel>,
  fontSize: TextUnit,
  color: Color,
  modifier: Modifier = Modifier,
  maxLines: Int = 1,
) {
  val defaultBackground = colors.noteTagBackground
  val defaultText = colors.noteTagText
  val tagged =
    remember(notes, tags, defaultBackground, defaultText) {
      taggedText(notes, tags, defaultBackground, defaultText)
    }
  var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

  Text(
    modifier =
      modifier.drawBehind {
        val result = layout ?: return@drawBehind
        tagged.chips.fastForEach { drawChip(result, it) }
      },
    text = tagged.text,
    fontSize = fontSize,
    color = color,
    maxLines = maxLines,
    overflow = Ellipsis,
    onTextLayout = { layout = it },
  )
}

// Each chip's range takes a padding space at either end, so the chip drawn behind it has room
private fun taggedText(
  notes: String,
  tags: List<TagLabel>,
  defaultBackground: Color,
  defaultText: Color,
): TaggedText {
  val chips = mutableListOf<Chip>()
  val text = buildAnnotatedString {
    var index = 0
    tagRangesInNotes(notes).fastForEach { range ->
      append(notes.substring(index, range.first))
      val token = notes.substring(range)
      val tagColor = tags.firstOrNull { it.name.equals(token.drop(1), ignoreCase = true) }?.color
      val start = length
      append(CHIP_PADDING)
      withStyle(
        SpanStyle(
          color = tagColor?.contrastingTextColor() ?: defaultText,
          fontWeight = SemiBold,
        ),
      ) {
        append(token)
      }
      append(CHIP_PADDING)
      chips += Chip(start, length, tagColor ?: defaultBackground)
      index = range.last + 1
    }
    append(notes.substring(index))
  }
  return TaggedText(text, chips)
}

// One rounded box per line the chip covers, in case a long tag wraps
private fun DrawScope.drawChip(layout: TextLayoutResult, chip: Chip) {
  val inset = ChipInset.toPx()
  for (line in layout.getLineForOffset(chip.start)..layout.getLineForOffset(chip.end - 1)) {
    val lineStart = maxOf(chip.start, layout.getLineStart(line))
    val lineEnd = minOf(chip.end, layout.getLineEnd(line, visibleEnd = true))
    if (lineEnd <= lineStart) continue

    val left = layout.getHorizontalPosition(lineStart, usePrimaryDirection = true)
    val right = layout.getHorizontalPosition(lineEnd, usePrimaryDirection = true)
    val top = layout.getLineTop(line) + inset
    val bottom = layout.getLineBottom(line) - inset
    drawRoundRect(
      color = chip.background,
      topLeft = Offset(left, top),
      size = Size(right - left, bottom - top),
      cornerRadius = CornerRadius((bottom - top) / 2),
    )
  }
}

// Non-breaking, so the padding never wraps away from its tag
private const val CHIP_PADDING = " "
private val ChipInset = 1.dp

@Preview
@Composable
private fun PreviewTaggedNotes(
  @PreviewParameter(TaggedNotesProvider::class) params: ColoredParams<String>,
) =
  PreviewWithColoredParams(params) {
    TaggedNotes(
      notes = this,
      tags = PREVIEW_TAGS,
      fontSize = ValueSize,
      color = colors.pageText,
      maxLines = 2,
    )
  }

private val PREVIEW_TAGS =
  persistentListOf(
    TagLabel(TagId("a"), "food", Color(0xFF3CB371)),
    TagLabel(TagId("b"), "holiday", Color(0xFFFFD700)),
  )

private class TaggedNotesProvider :
  ColoredParameterProvider<String>(
    "Lunch with #food and #holiday",
    "#untagged colour",
    "No tags here",
    "A long note that wraps over more than one line, with #food #holiday at the end",
  )
