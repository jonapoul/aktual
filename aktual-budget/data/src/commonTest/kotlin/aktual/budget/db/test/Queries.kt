package aktual.budget.db.test

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.model.Account
import aktual.budget.db.model.Bank
import aktual.budget.db.model.Meta
import aktual.budget.db.model.PayeeMapping
import aktual.budget.db.model.Rule
import aktual.budget.db.model.Schedule
import aktual.budget.db.model.ScheduleJsonPath
import aktual.budget.db.model.ScheduleNextDate
import aktual.budget.model.AccountId
import aktual.budget.model.Operator
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import aktual.budget.model.ScheduleId
import aktual.budget.model.ScheduleJsonPathIndex
import aktual.budget.model.ScheduleNextDateId
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray

internal suspend fun BudgetDatabase.getAccountById(id: AccountId): Account? = accounts().getById(id)

internal suspend fun BudgetDatabase.insertAccounts(vararg accounts: Account) =
  accounts().insert(accounts.toList())

internal suspend fun BudgetDatabase.insertBanks(vararg banks: Bank) = banks().insert(banks.toList())

internal suspend fun BudgetDatabase.getMetaValue(key: String): String? = meta().getValue(key)

internal suspend fun BudgetDatabase.insertMeta(key: String, value: String) =
  meta().insert(Meta(key, value))

internal suspend fun BudgetDatabase.insertRule(
  id: String,
  stage: RuleStage? = null,
  conditions: String?,
  actions: String?,
  tombstone: Boolean? = false,
  conditionsOp: Operator? = Operator.And,
) =
  rules()
    .insert(
      Rule(
        id = RuleId(id),
        stage = stage,
        conditions = conditions?.toJsonArray(),
        actions = actions?.toJsonArray(),
        tombstone = tombstone,
        conditionsOp = conditionsOp,
      )
    )

internal suspend fun BudgetDatabase.insertScheduleJsonPaths(
  scheduleId: String,
  payee: Int,
  account: Int,
  amount: Int,
  date: Int,
) =
  scheduleJsonPaths()
    .insert(
      ScheduleJsonPath(
        scheduleId = ScheduleId(scheduleId),
        payee = ScheduleJsonPathIndex(payee),
        account = ScheduleJsonPathIndex(account),
        amount = ScheduleJsonPathIndex(amount),
        date = ScheduleJsonPathIndex(date),
      )
    )

internal suspend fun BudgetDatabase.insertScheduleNextDate(
  id: String,
  scheduleId: String,
  localDate: String,
  localInstant: Long,
  baseDate: String,
  baseInstant: Long,
) =
  schedulesNextDate()
    .insert(
      ScheduleNextDate(
        id = ScheduleNextDateId(id),
        scheduleId = ScheduleId(scheduleId),
        localNextDate = LocalDate.parse(localDate),
        localNextDateTs = Instant.fromEpochMilliseconds(localInstant),
        baseNextDate = LocalDate.parse(baseDate),
        baseNextDateTs = Instant.fromEpochMilliseconds(baseInstant),
      )
    )

internal suspend fun BudgetDatabase.insertSchedule(
  id: String,
  ruleId: String,
  completed: Boolean,
  postsTransaction: Boolean,
  tombstone: Boolean,
  name: String,
  active: Boolean = false,
) =
  schedules()
    .insert(
      Schedule(
        id = ScheduleId(id),
        rule = RuleId(ruleId),
        active = active,
        completed = completed,
        postsTransaction = postsTransaction,
        tombstone = tombstone,
        name = name,
      )
    )

internal suspend fun BudgetDatabase.insertPayeeMapping(id: String, targetId: String) =
  payeeMapping().insert(PayeeMapping(id = PayeeId(id), targetId = PayeeId(targetId)))

internal suspend fun BudgetDatabase.insertPayeeMapping(id: String) = insertPayeeMapping(id, id)

private fun String.toJsonArray() = Json.parseToJsonElement(this).jsonArray
