package aktual.budget.schedules.vm.edit

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.PayeeId
import aktual.budget.model.RecurConfig
import androidx.compose.runtime.Immutable
import kotlin.math.abs
import kotlinx.datetime.LocalDate

/** The editable parts of a schedule */
@Immutable
data class ScheduleForm(
  val name: String,
  val payee: PayeeId?,
  val account: AccountId?,
  val amount: ScheduleAmount,
  val date: ScheduleDate,
  val postsTransaction: Boolean,
)

/** Amounts are signed: negative for payments, positive for deposits */
@Immutable
sealed interface ScheduleAmount {
  data class Exactly(val amount: Amount) : ScheduleAmount

  // Upstream matches within 7.5% either side
  data class Approximately(val amount: Amount) : ScheduleAmount

  data class Between(val from: Amount, val to: Amount) : ScheduleAmount
}

val ScheduleAmount.isDeposit: Boolean
  get() =
    when (this) {
      is Exactly -> amount > Zero
      is Approximately -> amount > Zero
      is Between -> from > Zero || to > Zero
    }

// Flips the sign of every amount so it reads as a payment or a deposit
fun ScheduleAmount.withDeposit(deposit: Boolean): ScheduleAmount {
  fun Amount.signed(): Amount = if (this > Zero == deposit || this == Zero) this else -this
  return when (this) {
    is Exactly -> ScheduleAmount.Exactly(amount.signed())
    is Approximately -> ScheduleAmount.Approximately(amount.signed())
    is Between -> ScheduleAmount.Between(from.signed(), to.signed())
  }
}

@Immutable
sealed interface ScheduleDate {
  val start: LocalDate

  data class Once(val date: LocalDate) : ScheduleDate {
    override val start = date
  }

  data class Recurring(val config: RecurConfig) : ScheduleDate {
    override val start = config.start
  }
}

// The same starting config upstream gives a new schedule, see ScheduleEditModal.tsx
fun defaultRecurConfig(start: LocalDate): RecurConfig =
  RecurConfig(
    frequency = Monthly,
    start = start,
    interval = 1,
    patterns = emptyList(),
    skipWeekend = false,
    weekendSolveMode = After,
    endMode = Never,
    endOccurrences = 1,
    endDate = start,
  )

@Immutable data class NamedEntity<T : Any>(val id: T, val name: String)

/**
 * Reads a typed, unsigned amount like "1,200.50" or "1.200,50". The last separator counts as the
 * decimal point when one or two digits follow it, otherwise separators are ignored.
 */
fun parseAmountInput(text: String): Amount? {
  val cleaned = text.filter { it.isDigit() || it == '.' || it == ',' }
  if (cleaned.none { it.isDigit() }) return null

  val separator = cleaned.indexOfLast { it == '.' || it == ',' }
  val decimals = cleaned.length - separator - 1
  val hasFraction = separator >= 0 && decimals in 0..MAX_DECIMALS
  val whole = if (hasFraction) cleaned.substring(0, separator) else cleaned
  val fraction = if (hasFraction) cleaned.substring(separator + 1) else ""

  val digits = whole.filter { it.isDigit() }.ifEmpty { "0" } + fraction.padEnd(MAX_DECIMALS, '0')
  return digits.toLongOrNull()?.let { Amount(it) }
}

/** The unsigned amount as plain text to edit, like "1200.50", or blank for zero */
fun Amount.toInputText(): String {
  val cents = abs(toLong())
  if (cents == 0L) return ""
  return "${cents / CENTS}.${(cents % CENTS).toString().padStart(MAX_DECIMALS, '0')}"
}

private const val MAX_DECIMALS = 2
private const val CENTS = 100
