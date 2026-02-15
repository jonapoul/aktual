package aktual.budget.db.test

import aktual.budget.db.Accounts
import aktual.budget.db.Banks
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.withResult
import aktual.budget.db.withoutResult
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

internal suspend fun BudgetDatabase.getAccountById(id: AccountId): Accounts? =
  accountsQueries.withResult { getById(id).executeAsOneOrNull() }

internal suspend fun BudgetDatabase.insertAccounts(vararg accounts: Accounts) =
  accountsQueries.withoutResult {
    accounts.forEach { account ->
      with(account) {
        insert(
          id = id,
          account_id = account_id,
          name = name,
          official_name = official_name,
          bank = bank,
          offbudget = offbudget,
          account_sync_source = account_sync_source,
        )
      }
    }
  }

internal suspend fun BudgetDatabase.insertBanks(vararg banks: Banks) =
  banksQueries.withoutResult { banks.forEach { bank -> with(bank) { insert(id, bank_id, name) } } }

internal suspend fun BudgetDatabase.getMetaValue(key: String): String? =
  metaQueries.withResult { getValue(key).executeAsOneOrNull()?.value_ }

internal suspend fun BudgetDatabase.insertMeta(key: String, value: String) =
  metaQueries.withoutResult { insert(key, value) }

internal suspend fun BudgetDatabase.insertRule(
  id: String,
  stage: RuleStage? = null,
  conditions: String?,
  actions: String?,
  tombstone: Boolean? = false,
  conditionsOp: Operator? = Operator.And,
) =
  rulesQueries.withResult {
    insert(
      id = RuleId(id),
      stage = stage,
      conditions = conditions?.toJsonArray(),
      actions = actions?.toJsonArray(),
      tombstone = tombstone,
      conditions_op = conditionsOp,
    )
  }

internal suspend fun BudgetDatabase.insertScheduleJsonPaths(
  scheduleId: String,
  payee: Int,
  account: Int,
  amount: Int,
  date: Int,
) =
  schedulesJsonPathsQueries.withResult {
    insert(
      schedule_id = ScheduleId(scheduleId),
      payee = ScheduleJsonPathIndex(payee),
      account = ScheduleJsonPathIndex(account),
      amount = ScheduleJsonPathIndex(amount),
      date = ScheduleJsonPathIndex(date),
    )
  }

internal suspend fun BudgetDatabase.insertScheduleNextDate(
  id: String,
  scheduleId: String,
  localDate: String,
  localInstant: Long,
  baseDate: String,
  baseInstant: Long,
) =
  schedulesNextDateQueries.withResult {
    insert(
      id = ScheduleNextDateId(id),
      schedule_id = ScheduleId(scheduleId),
      local_next_date = LocalDate.parse(localDate),
      local_next_date_ts = Instant.fromEpochMilliseconds(localInstant),
      base_next_date = LocalDate.parse(baseDate),
      base_next_date_ts = Instant.fromEpochMilliseconds(baseInstant),
    )
  }

internal suspend fun BudgetDatabase.insertSchedule(
  id: String,
  ruleId: String,
  completed: Boolean,
  postsTransaction: Boolean,
  tombstone: Boolean,
  name: String,
  active: Boolean = false,
) =
  schedulesQueries.withResult {
    insert(
      id = ScheduleId(id),
      rule = RuleId(ruleId),
      active = active,
      completed = completed,
      posts_transaction = postsTransaction,
      tombstone = tombstone,
      name = name,
    )
  }

internal suspend fun BudgetDatabase.insertPayeeMapping(id: String, targetId: String) =
  payeeMappingQueries.withResult { insert(id = PayeeId(id), targetId = PayeeId(targetId)) }

internal suspend fun BudgetDatabase.insertPayeeMapping(id: String) = insertPayeeMapping(id, id)

private fun String.toJsonArray() = Json.parseToJsonElement(this).jsonArray
