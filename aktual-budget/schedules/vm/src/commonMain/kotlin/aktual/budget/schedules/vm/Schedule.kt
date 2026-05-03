package aktual.budget.schedules.vm

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.AmountOperator
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.model.UpcomingLength
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable
data class Schedule(
  val id: ScheduleId,
  val name: String?,
  val ruleId: RuleId,
  val nextDate: LocalDate,
  val isCompleted: Boolean,
  val postsTransaction: Boolean,
  val customUpcomingLength: UpcomingLength?,
  val payeeId: PayeeId,
  val payeeName: String,
  val accountId: AccountId,
  val accountName: String,
  val amount: Amount,
  val amountOp: AmountOperator,
  val date: LocalDate,
  val status: ScheduleStatus,
)
