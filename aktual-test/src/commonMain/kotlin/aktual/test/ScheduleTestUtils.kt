package aktual.test

import aktual.budget.db.BudgetDatabase
import aktual.budget.model.AccountId
import aktual.budget.model.Condition
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.model.ScheduleJsonPathIndex
import aktual.budget.model.ScheduleNextDateId
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

// Inserts a fixed-date schedule with its own payee, account and rule
@Suppress("LongParameterList")
suspend fun BudgetDatabase.insertSchedule(
  id: String,
  name: String?,
  payee: String,
  account: String,
  nextDate: LocalDate = LocalDate(2026, 5, 1),
  amount: Long = -1000,
  dateConditionIndex: Int = 2,
  completed: Boolean = false,
) {
  val payeeId = PayeeId("$id-payee")
  val accountId = AccountId("$id-account")
  val ruleId = RuleId("$id-rule")
  val scheduleId = ScheduleId(id)

  accountsQueries.insert(
    id = accountId,
    account_id = null,
    name = account,
    official_name = null,
    bank = null,
    offbudget = false,
    account_sync_source = null,
  )
  payeesQueries.insert(
    id = payeeId,
    name = payee,
    category = null,
    tombstone = false,
    transfer_acct = null,
    favorite = false,
    learn_categories = 1,
  )
  payeeMappingQueries.insert(id = payeeId, targetId = payeeId)

  val conditions =
    """
    [
      {"op": "is", "field": "description", "value": "${payeeId.value}"},
      {"op": "is", "field": "acct", "value": "${accountId.value}"},
      {"op": "is", "field": "date", "value": "$nextDate"},
      {"op": "is", "field": "amount", "value": $amount}
    ]
    """
      .trimIndent()
  rulesQueries.insert(
    id = ruleId,
    stage = null,
    conditions = Json.decodeFromString(ListSerializer(Condition.serializer()), conditions),
    actions = emptyList(),
    tombstone = false,
    conditions_op = null,
  )

  schedulesJsonPathsQueries.insert(
    schedule_id = scheduleId,
    payee = ScheduleJsonPathIndex(0),
    account = ScheduleJsonPathIndex(1),
    amount = ScheduleJsonPathIndex(3),
    date = ScheduleJsonPathIndex(dateConditionIndex),
  )
  val timestamp = Instant.fromEpochMilliseconds(0)
  schedulesNextDateQueries.insert(
    id = ScheduleNextDateId("$id-next"),
    schedule_id = scheduleId,
    local_next_date = nextDate,
    local_next_date_ts = timestamp,
    base_next_date = nextDate,
    base_next_date_ts = timestamp,
  )
  schedulesQueries.insert(
    id = scheduleId,
    rule = ruleId,
    active = true,
    completed = completed,
    posts_transaction = false,
    tombstone = false,
    name = name,
  )
}
