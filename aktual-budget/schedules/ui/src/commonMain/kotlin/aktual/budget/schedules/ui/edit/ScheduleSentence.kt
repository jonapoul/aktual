package aktual.budget.schedules.ui.edit

import aktual.budget.model.Amount
import aktual.budget.model.RecurConfig
import aktual.budget.schedules.vm.edit.EditScheduleState
import aktual.budget.schedules.vm.edit.ScheduleAmount
import aktual.budget.schedules.vm.edit.ScheduleDate
import aktual.budget.schedules.vm.edit.isDeposit
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import aktual.core.ui.frequencyDescription
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import kotlinx.datetime.LocalDate

@Immutable private data class Token(val text: String, val part: SentencePart, val color: Color)

/**
 * The schedule as one sentence, e.g. "Pay £1,200.00 to Landlord from Current account every month on
 * the 1st, starting 1 Oct 2026." While editing, each highlighted part is drawn as a chip that opens
 * the editor for that part, with the chip for [activePart] filled in.
 */
@Composable
internal fun ScheduleSentence(
  state: EditScheduleState.Loaded,
  activePart: SentencePart?,
  onClick: (SentencePart) -> Unit,
  modifier: Modifier = Modifier,
  style: TextStyle = typography.titleSmall.copy(lineHeight = SentenceLineHeight),
) {
  val text = rememberScheduleSentence(state, activePart, onClick)
  val chipColors =
    ChipColors(
      background = colors.pillBackgroundSelected.copy(alpha = CHIP_BACKGROUND_ALPHA),
      activeBackground = colors.pillBackgroundSelected,
      border = colors.pageTextLinkLight,
    )
  var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

  Text(
    modifier =
      modifier.drawBehind {
        val result = layout ?: return@drawBehind
        val laidOut = result.layoutInput.text
        laidOut.getLinkAnnotations(0, laidOut.length).fastForEach { range ->
          val isActive = (range.item as? LinkAnnotation.Clickable)?.tag == activePart?.name
          drawChip(result, range.start, range.end, isActive, chipColors)
        }
      },
    text = text,
    style = style.copy(lineHeightStyle = LineHeightStyle(Center, None)),
    onTextLayout = { layout = it },
  )
}

@Immutable
private data class ChipColors(val background: Color, val activeBackground: Color, val border: Color)

// One rounded box per line the range covers, so a part that wraps gets a box on each line
private fun DrawScope.drawChip(
  layout: TextLayoutResult,
  start: Int,
  end: Int,
  isActive: Boolean,
  colors: ChipColors,
) {
  val gap = ChipGap.toPx()
  val radius = CornerRadius(ChipRadius.toPx())
  val strokeWidth = (if (isActive) ActiveBorderWidth else BorderWidth).toPx()
  val stroke =
    if (isActive) {
      Stroke(width = strokeWidth)
    } else {
      val dash = DashLength.toPx()
      Stroke(width = strokeWidth, pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)))
    }

  // Where the chip wraps, its ends get the same padding as the padding spaces at its real ends
  val padding =
    layout.getHorizontalPosition(start + 1, usePrimaryDirection = true) -
      layout.getHorizontalPosition(start, usePrimaryDirection = true)

  for (line in layout.getLineForOffset(start)..layout.getLineForOffset(end - 1)) {
    val lineStart = maxOf(start, layout.getLineStart(line))
    val lineEnd = minOf(end, layout.getLineEnd(line, visibleEnd = true))
    if (lineEnd <= lineStart) continue

    val startPadding = if (lineStart > start) padding else 0f
    val endPadding = if (lineEnd < end) padding else 0f
    val left = layout.getHorizontalPosition(lineStart, usePrimaryDirection = true) - startPadding
    val right = layout.getHorizontalPosition(lineEnd, usePrimaryDirection = true) + endPadding
    // Fills the line, leaving a small gap to the chips around it
    val top = layout.getLineTop(line) + gap / 2
    val bottom = layout.getLineBottom(line) - gap / 2
    val topLeft = Offset(left - gap / 2, top)
    val size = Size(right - left + gap, bottom - top)

    drawRoundRect(
      color = if (isActive) colors.activeBackground else colors.background,
      topLeft = topLeft,
      size = size,
      cornerRadius = radius,
    )
    val inset = strokeWidth / 2
    drawRoundRect(
      color = colors.border,
      topLeft = topLeft + Offset(inset, inset),
      size = Size(size.width - strokeWidth, size.height - strokeWidth),
      cornerRadius = radius,
      style = stroke,
    )
  }
}

@Composable
private fun rememberScheduleSentence(
  state: EditScheduleState.Loaded,
  activePart: SentencePart?,
  onClick: (SentencePart) -> Unit,
): AnnotatedString {
  val form = state.form
  val isEditing = state.isEditing
  val isDeposit = form.amount.isDeposit

  val strongColor = colors.pageText
  val activeColor = colors.buttonPrimaryText
  val amountColor =
    when {
      isEditing -> strongColor
      isDeposit -> colors.numberPositive
      else -> colors.numberNegative
    }
  val plainColor = colors.pageTextSubdued

  val amount = Token(form.amount.text(), SentencePart.Amount, amountColor)
  val payee = Token(state.payeeName ?: Strings.editSchedulePayeeUnset, Payee, strongColor)
  val account = Token(state.accountName ?: Strings.editScheduleAccountUnset, Account, strongColor)

  val sentence =
    if (isDeposit) {
      Strings.editScheduleSentenceDeposit(AMOUNT, PAYEE, ACCOUNT, WHEN)
    } else {
      Strings.editScheduleSentencePayment(AMOUNT, PAYEE, ACCOUNT, WHEN)
    }

  // The date part is its own template, with the repeat and the start date as separate tokens
  val date = form.date
  val whenTemplate =
    when (date) {
      is Once -> Strings.editScheduleSentenceOnce(DATE)
      is Recurring -> Strings.editScheduleSentenceRecurring(REPEAT, DATE)
    }
  val dateToken =
    when (date) {
      is Once -> Token(date.date.formatted(), When, strongColor)
      is Recurring -> Token(date.config.start.formatted(), When, strongColor)
    }
  val repeatToken =
    (date as? ScheduleDate.Recurring)?.let { Token(it.config.repeatText(), When, strongColor) }
  val template = sentence.replace(WHEN, whenTemplate)

  val weekend =
    (date as? ScheduleDate.Recurring)
      ?.config
      ?.takeIf { it.skipWeekend == true }
      ?.let { config ->
        val text =
          when (config.weekendSolveMode) {
            Before -> Strings.editScheduleWeekendBefore
            After,
            Unknown,
            null -> Strings.editScheduleWeekendAfter
          }
        Strings.editScheduleSentenceWeekend(WEEKEND) to Token(text, When, strongColor)
      }

  val latestOnClick = rememberUpdatedState(onClick)
  return remember(
    template,
    amount,
    payee,
    account,
    dateToken,
    repeatToken,
    weekend,
    isEditing,
    activePart,
    plainColor,
    activeColor,
  ) {
    val styles =
      SentenceStyles(isEditing, activePart, plainColor, activeColor) { latestOnClick.value(it) }
    buildAnnotatedString {
      appendTemplate(
        text = template,
        tokens =
          buildMap {
            put(AMOUNT, amount)
            put(PAYEE, payee)
            put(ACCOUNT, account)
            put(DATE, dateToken)
            if (repeatToken != null) put(REPEAT, repeatToken)
          },
        styles = styles,
      )
      if (weekend != null) {
        append(" ")
        appendTemplate(weekend.first, mapOf(WEEKEND to weekend.second), styles)
      }
    }
  }
}

private data class SentenceStyles(
  val isEditing: Boolean,
  val activePart: SentencePart?,
  val plainColor: Color,
  val activeColor: Color,
  val onClick: (SentencePart) -> Unit,
)

// Appends the text, swapping each placeholder for its token
private fun AnnotatedString.Builder.appendTemplate(
  text: String,
  tokens: Map<String, Token>,
  styles: SentenceStyles,
) {
  var remaining = text
  while (remaining.isNotEmpty()) {
    val next =
      tokens.keys
        .mapNotNull { key -> remaining.indexOf(key).takeIf { it >= 0 }?.let { it to key } }
        .minByOrNull { it.first }

    if (next == null) {
      withStyle(SpanStyle(color = styles.plainColor)) { append(remaining) }
      return
    }

    val (index, key) = next
    withStyle(SpanStyle(color = styles.plainColor)) { append(remaining.substring(0, index)) }
    appendToken(tokens.getValue(key), styles)
    remaining = remaining.substring(index + key.length)
  }
}

private fun AnnotatedString.Builder.appendToken(token: Token, styles: SentenceStyles) {
  val style = SpanStyle(color = token.color, fontWeight = SemiBold)
  if (!styles.isEditing) {
    withStyle(style) { append(token.text) }
    return
  }

  // The chip is drawn behind the link's range, so the padding spaces sit inside it
  val color = if (token.part == styles.activePart) styles.activeColor else token.color
  val link =
    LinkAnnotation.Clickable(
      tag = token.part.name,
      styles = TextLinkStyles(style = style.copy(color = color)),
      linkInteractionListener = { styles.onClick(token.part) },
    )
  withLink(link) { append(CHIP_PADDING + token.text + CHIP_PADDING) }
}

@Composable
private fun ScheduleAmount.text(): String =
  when (this) {
    is Exactly -> amount.unsigned()
    is Approximately -> Strings.editScheduleAmountApprox(amount.unsigned())
    is Between ->
      Strings.editScheduleAmountBetween(minOf(from, to).unsigned(), maxOf(from, to).unsigned())
  }

// The sentence says whether it's paid or received, so the sign is left off
@Composable
@ReadOnlyComposable
private fun Amount.unsigned(): String =
  (if (this < Zero) -this else this).formattedString(includeSign = false)

@Composable
private fun RecurConfig.repeatText(): String {
  val frequency = frequencyDescription().replaceFirstChar { it.lowercase() }
  val end =
    when (endMode) {
      AfterNOccurrences -> {
        val count = endOccurrences ?: 1
        Plurals.editScheduleSentenceTimes(count, count)
      }
      OnDate -> {
        endDate?.let { Strings.editScheduleSentenceUntil(it.formatted()) }
      }
      Never,
      Unknown,
      null -> {
        null
      }
    }
  return if (end == null) frequency else Strings.recurWithEnd(frequency, end)
}

// Placeholders for the tokens, swapped out after the localised sentence is formatted. They're
// from the private use area so they can't clash with real text
private const val AMOUNT = ""
private const val PAYEE = ""
private const val ACCOUNT = ""
private const val WHEN = ""
private const val WEEKEND = ""
private const val REPEAT = ""
private const val DATE = ""

// No-break so a chip's padding never wraps away from its text
private const val CHIP_PADDING = " "

private const val CHIP_BACKGROUND_ALPHA = 0.35f
private val ChipRadius = 6.dp
private val ChipGap = 5.dp
private val BorderWidth = 1.dp
private val ActiveBorderWidth = 2.dp
private val DashLength = 4.dp
private val SentenceLineHeight = 44.sp

@Preview(widthDp = 390)
@Composable
private fun PreviewScheduleSentence(
  @PreviewParameter(ScheduleSentenceProvider::class) params: ColoredParams<SentencePreview>,
) =
  PreviewWithColoredParams(params) {
    ScheduleSentence(
      modifier = Modifier.padding(16.dp),
      state = state,
      activePart = activePart,
      onClick = {},
    )
  }

private data class SentencePreview(
  val state: EditScheduleState.Loaded,
  val activePart: SentencePart? = null,
)

private val PreviewEditing = PreviewLoaded.copy(isEditing = true)

private val PreviewLongNames =
  PreviewEditing.copy(
    form =
      PreviewEditing.form.copy(
        amount = ScheduleAmount.Approximately(Amount(-14_000L)),
        date =
          ScheduleDate.Recurring(
            RecurConfig(
              frequency = Monthly,
              start = LocalDate(2026, 3, 30),
              interval = 2,
              skipWeekend = true,
              weekendSolveMode = After,
              endMode = OnDate,
              endDate = LocalDate(2028, 3, 30),
            ),
          ),
      ),
    payeeName = "Student Loans Company",
    accountName = "Student Loan Plan 2",
  )

private val PreviewDeposit =
  PreviewEditing.copy(
    form =
      PreviewEditing.form.copy(
        amount = ScheduleAmount.Between(Amount(4_000L), Amount(6_000L)),
        date = ScheduleDate.Once(LocalDate(2026, 11, 5)),
      ),
    payeeName = null,
  )

private class ScheduleSentenceProvider :
  ColoredParameterProvider<SentencePreview>(
    SentencePreview(PreviewLoaded),
    SentencePreview(PreviewEditing),
    SentencePreview(PreviewEditing, activePart = SentencePart.Amount),
    SentencePreview(PreviewLongNames, activePart = When),
    SentencePreview(PreviewDeposit),
  )
