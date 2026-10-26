package aktual.budget.transactions.ui.edit

import aktual.budget.model.TagId
import aktual.budget.model.tagRangesInNotes
import aktual.budget.tags.domain.TagLabel
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ChipSize
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParameters
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.contrastingTextColor
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable private data class Chip(val start: Int, val end: Int, val background: Color)

@Immutable private data class TaggedText(val text: AnnotatedString, val chips: List<Chip>)

@Immutable private data class MeasuredText(val layout: TextLayoutResult, val chips: List<Chip>)

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
  val style = LocalTextStyle.current.merge(color = color, fontSize = fontSize)
  val measurer = rememberTextMeasurer()
  var measured by remember { mutableStateOf<MeasuredText?>(null) }

  Layout(
    modifier =
      modifier
        .semantics { text = tagged.text }
        .drawBehind {
          val result = measured ?: return@drawBehind
          result.chips.fastForEach { drawChip(result.layout, it) }
          drawText(result.layout)
        },
  ) { _, constraints ->
    val result = measurer.measureTagged(tagged, style, maxLines, constraints, layoutDirection, this)
    measured = result
    layout(result.layout.size.width, result.layout.size.height) {}
  }
}

// If the text overflows, cut it before the first chip that can't fit whole, so the ellipsis never
// lands partway through a chip
private fun TextMeasurer.measureTagged(
  tagged: TaggedText,
  style: TextStyle,
  maxLines: Int,
  constraints: Constraints,
  layoutDirection: LayoutDirection,
  density: Density,
): MeasuredText {
  fun measure(text: AnnotatedString) =
    measure(
      text = text,
      style = style,
      overflow = Ellipsis,
      maxLines = maxLines,
      constraints = constraints,
      layoutDirection = layoutDirection,
      density = density,
    )

  val full = measure(tagged.text)
  if (!full.hasVisualOverflow) return MeasuredText(full, tagged.chips)

  val index = tagged.chips.indexOfFirst { measure(tagged.text.truncated(it.end)).hasVisualOverflow }
  if (index < 0) return MeasuredText(full, tagged.chips)

  val cut = tagged.text.trimmedEnd(tagged.chips[index].start)
  return MeasuredText(measure(tagged.text.truncated(cut)), tagged.chips.subList(0, index))
}

private fun AnnotatedString.truncated(end: Int): AnnotatedString =
  subSequence(0, end) + AnnotatedString(ELLIPSIS)

private fun AnnotatedString.trimmedEnd(end: Int): Int {
  var index = end
  while (index > 0 && text[index - 1] == ' ') index--
  return index
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
      cornerRadius = CornerRadius(ChipSize.Small.radius.toPx()),
    )
  }
}

// Non-breaking, so the padding never wraps away from its tag
private const val CHIP_PADDING = " "
private const val ELLIPSIS = "\u2026"
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

// Narrowing widths, so the cut lands on each chip in turn
@Preview
@Composable
private fun PreviewTaggedNotesTruncated(
  @PreviewParameter(ColoredParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      PREVIEW_WIDTHS.fastForEach { width ->
        TaggedNotes(
          modifier = Modifier.width(width).border(1.dp, colors.cardBorder),
          notes = "Lunch with #food and #holiday",
          tags = PREVIEW_TAGS,
          fontSize = ValueSize,
          color = colors.pageText,
        )
      }
    }
  }

private val PREVIEW_WIDTHS = (80..240 step 20).map { it.dp }

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
