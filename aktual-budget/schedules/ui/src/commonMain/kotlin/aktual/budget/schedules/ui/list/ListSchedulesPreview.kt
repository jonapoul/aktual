package aktual.budget.schedules.ui.list

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.Operator
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.schedules.vm.Schedule
import aktual.budget.schedules.vm.ScheduleStatus
import kotlinx.datetime.LocalDate

internal object ListSchedulesPreview {
  val scheduleA =
    Schedule(
      id = ScheduleId("abc-123"),
      name = "Monthly Rent",
      ruleId = RuleId("rule-1"),
      nextDate = LocalDate(2026, 5, 1),
      isCompleted = false,
      postsTransaction = false,
      customUpcomingLength = null,
      payeeId = PayeeId("payee-1"),
      payeeName = "Landlord Inc.",
      accountId = AccountId("account-1"),
      accountName = "Checking",
      amount = Amount(-150000),
      amountOp = Operator.Is,
      date = LocalDate(2026, 5, 1),
      status = ScheduleStatus.Upcoming,
    )

  val scheduleB =
    Schedule(
      id = ScheduleId("def-456"),
      name = null,
      ruleId = RuleId("rule-2"),
      nextDate = LocalDate(2026, 4, 15),
      isCompleted = false,
      postsTransaction = false,
      customUpcomingLength = null,
      payeeId = PayeeId("payee-2"),
      payeeName = "Electricity Co.",
      accountId = AccountId("account-1"),
      accountName = "Checking",
      amount = Amount(-8000),
      amountOp = Operator.IsApprox,
      date = LocalDate(2026, 4, 15),
      status = ScheduleStatus.Missed,
    )
}
