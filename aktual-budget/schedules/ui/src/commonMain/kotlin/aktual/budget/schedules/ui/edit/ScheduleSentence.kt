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
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import aktual.core.ui.frequencyDescription
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle

/** The parts of the sentence that open an editor when tapped */
internal enum class SentencePart {
  Amount,
  Payee,
  Account,
  When,
}

@Immutable private data class Token(val text: String, val part: SentencePart, val color: Color)

/**
 * The schedule as one sentence, e.g. "Pay £1,200.00 to Landlord from Current account every month on
 * the 1st, starting 1 Oct 2026." While editing, each highlighted part is a link that opens the
 * editor for that part.
 */
@Composable
internal fun rememberScheduleSentence(
  state: EditScheduleState.Loaded,
  onClick: (SentencePart) -> Unit,
): AnnotatedString {
  val form = state.form
  val isEditing = state.isEditing
  val isDeposit = form.amount.isDeposit

  val strongColor = colors.pageText
  val amountColor = if (isDeposit) colors.numberPositive else colors.numberNegative
  val plainColor = colors.pageTextSubdued
  val linkBackground = colors.pillBackgroundSelected.copy(alpha = LINK_BACKGROUND_ALPHA)

  val amount = Token(form.amount.text(), SentencePart.Amount, amountColor)
  val payee = Token(state.payeeName ?: Strings.editSchedulePayeeUnset, Payee, strongColor)
  val account = Token(state.accountName ?: Strings.editScheduleAccountUnset, Account, strongColor)
  val date = Token(form.date.text(), When, strongColor)

  val template =
    if (isDeposit) {
      Strings.editScheduleSentenceDeposit(AMOUNT, PAYEE, ACCOUNT, WHEN)
    } else {
      Strings.editScheduleSentencePayment(AMOUNT, PAYEE, ACCOUNT, WHEN)
    }

  val weekend =
    (form.date as? ScheduleDate.Recurring)
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
    date,
    weekend,
    isEditing,
    plainColor,
    linkBackground,
  ) {
    val styles = SentenceStyles(isEditing, plainColor, linkBackground) { latestOnClick.value(it) }
    buildAnnotatedString {
      appendTemplate(
        text = template,
        tokens = mapOf(AMOUNT to amount, PAYEE to payee, ACCOUNT to account, WHEN to date),
        styles = styles,
      )
      if (weekend != null) {
        append(" ")
        appendTemplate(weekend.first, mapOf(WEEKEND to weekend.second), styles)
      }
    }
  }
}

private class SentenceStyles(
  val isEditing: Boolean,
  val plainColor: Color,
  val linkBackground: Color,
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

  val linkStyle = style.copy(background = styles.linkBackground, textDecoration = Underline)
  val link =
    LinkAnnotation.Clickable(
      tag = token.part.name,
      styles = TextLinkStyles(style = linkStyle),
      linkInteractionListener = { styles.onClick(token.part) },
    )
  withLink(link) { append(token.text) }
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
private fun ScheduleDate.text(): String =
  when (this) {
    is Once -> Strings.editScheduleSentenceOnce(date.formatted())
    is Recurring ->
      Strings.editScheduleSentenceRecurring(config.repeatText(), config.start.formatted())
  }

@Composable
private fun RecurConfig.repeatText(): String {
  val frequency = frequencyDescription().replaceFirstChar { it.lowercase() }
  val end =
    when (endMode) {
      AfterNOccurrences -> {
        val count = endOccurrences ?: 1
        Plurals.editScheduleSentenceTimes(count, count)
      }
      OnDate -> endDate?.let { Strings.editScheduleSentenceUntil(it.formatted()) }
      else -> null
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

private const val LINK_BACKGROUND_ALPHA = 0.35f
