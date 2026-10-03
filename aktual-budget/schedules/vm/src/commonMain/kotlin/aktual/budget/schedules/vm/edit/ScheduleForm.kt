package aktual.budget.schedules.vm.edit

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.PayeeId
import aktual.budget.model.RecurConfig
import androidx.compose.runtime.Immutable
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

// Flips the sign of every amount so it reads as a payment or a deposit, keeping a range ascending
fun ScheduleAmount.withDeposit(deposit: Boolean): ScheduleAmount {
  fun Amount.signed(): Amount = if (this > Zero == deposit || this == Zero) this else -this
  return when (this) {
    is Exactly -> {
      ScheduleAmount.Exactly(amount.signed())
    }
    is Approximately -> {
      ScheduleAmount.Approximately(amount.signed())
    }
    is Between -> {
      val a = from.signed()
      val b = to.signed()
      ScheduleAmount.Between(minOf(a, b), maxOf(a, b))
    }
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
    patterns = [],
    skipWeekend = false,
    weekendSolveMode = After,
    endMode = Never,
    endOccurrences = 1,
    endDate = start,
  )

@Immutable data class NamedEntity<T : Any>(val id: T, val name: String)
