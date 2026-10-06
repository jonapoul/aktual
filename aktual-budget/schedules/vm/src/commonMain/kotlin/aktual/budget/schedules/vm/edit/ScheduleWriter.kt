package aktual.budget.schedules.vm.edit

import aktual.budget.BudgetSyncController
import aktual.budget.db.DbJson
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.db.dao.DatabaseTables.SCHEDULES
import aktual.budget.db.dao.DatabaseTables.SCHEDULES_NEXT_DATE
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.model.ScheduleNextDateId
import aktual.budget.model.messageValue
import aktual.budget.model.nextDate
import aktual.budget.model.serialName
import aktual.budget.model.tombstone
import aktual.core.Calendar
import aktual.core.UuidGenerator
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonPrimitive
import logcat.logcat

internal class DuplicateScheduleNameException(name: String) :
  IllegalArgumentException("There is already a schedule named \"$name\"")

/**
 * Writes schedules the way packages/loot-core/src/server/schedules/app.ts does. Everything synced
 * goes through [BudgetSyncController], which also applies the changes to the local database. As
 * upstream, a new row's id is the message row, so there's no message for the id column itself.
 */
@Inject
internal class ScheduleWriter(
  private val scheduleDao: ScheduleDao,
  private val syncController: BudgetSyncController,
  private val uuidGenerator: UuidGenerator,
  private val calendar: Calendar,
  private val clock: Clock,
) {
  // createSchedule()
  suspend fun create(form: ScheduleForm): ScheduleId {
    val name = form.name.normalized()
    checkNameIsFree(name, id = null)

    val scheduleId = uuidGenerator(::ScheduleId)
    val ruleId = uuidGenerator(::RuleId)
    val nextDateId = uuidGenerator(::ScheduleNextDateId)
    val conditions = form.toConditions(existing = emptyList())
    val actions = listOf(RuleAction(value = JsonPrimitive(scheduleId.value), op = LinkSchedule))
    val nextDate = form.date.nextDate(calendar.today())
    val now = clock.now().toEpochMilliseconds()

    val rule = ruleId.toString()
    val next = nextDateId.toString()
    val schedule = scheduleId.toString()
    val changes = buildList {
      add(LocalChange(RULES, rule, "stage", Null))
      add(LocalChange(RULES, rule, "conditions", conditions.encode().messageValue()))
      add(LocalChange(RULES, rule, "actions", actions.encode().messageValue()))
      add(LocalChange(RULES, rule, "conditions_op", ConditionOp.And.serialName().messageValue()))
      add(LocalChange(RULES, rule, TOMBSTONE, false.messageValue()))

      add(LocalChange(SCHEDULES_NEXT_DATE, next, "schedule_id", schedule.messageValue()))
      add(LocalChange(SCHEDULES_NEXT_DATE, next, "local_next_date", nextDate.dateValue()))
      add(LocalChange(SCHEDULES_NEXT_DATE, next, "local_next_date_ts", MessageValue.Number(now)))
      add(LocalChange(SCHEDULES_NEXT_DATE, next, "base_next_date", nextDate.dateValue()))
      add(LocalChange(SCHEDULES_NEXT_DATE, next, "base_next_date_ts", MessageValue.Number(now)))
      add(LocalChange(SCHEDULES_NEXT_DATE, next, TOMBSTONE, false.messageValue()))

      add(LocalChange(SCHEDULES, schedule, "rule", rule.messageValue()))
      add(LocalChange(SCHEDULES, schedule, "name", name.messageValue()))
      add(
        LocalChange(SCHEDULES, schedule, "posts_transaction", form.postsTransaction.messageValue())
      )
      add(LocalChange(SCHEDULES, schedule, "completed", false.messageValue()))
      add(LocalChange(SCHEDULES, schedule, TOMBSTONE, false.messageValue()))
    }

    syncController.syncChanges(changes)
    scheduleDao.setJsonPaths(scheduleId, conditions.jsonPaths())
    logcat.i { "Created schedule $scheduleId" }
    return scheduleId
  }

  // updateSchedule(), with the conditions always passed in
  suspend fun update(
    id: ScheduleId,
    ruleId: RuleId,
    oldConditions: List<Condition>,
    oldActions: List<RuleAction>,
    form: ScheduleForm,
  ) {
    val name = form.name.normalized()
    checkNameIsFree(name, id)

    val conditions = mergeConditions(oldConditions, form.toConditions(oldConditions))
    val actions = syncAmountActions(conditions, oldActions)

    val rule = ruleId.toString()
    val schedule = id.toString()
    val changes = buildList {
      add(LocalChange(RULES, rule, "conditions", conditions.encode().messageValue()))
      if (actions != null) add(LocalChange(RULES, rule, "actions", actions.encode().messageValue()))

      // The next date only moves when the account or date changed. Upstream skips closed accounts
      // when moving schedules along, so switching away from one needs a fresh next date
      val old = oldConditions.scheduleConditions()
      val new = conditions.scheduleConditions()
      val accountChanged = old.account?.withoutType() != new.account?.withoutType()
      val dateChanged = old.date?.value != new.date?.value
      if (accountChanged || dateChanged) addAll(resetNextDate(id, form.date))

      add(LocalChange(SCHEDULES, schedule, "name", name.messageValue()))
      add(
        LocalChange(SCHEDULES, schedule, "posts_transaction", form.postsTransaction.messageValue())
      )
    }

    syncController.syncChanges(changes)
    scheduleDao.setJsonPaths(id, conditions.jsonPaths())
    logcat.i { "Updated schedule $id" }
  }

  // deleteSchedule()
  suspend fun delete(id: ScheduleId, ruleId: RuleId?) {
    val changes =
      listOfNotNull(
        ruleId?.let { tombstone(dataset = RULES, row = it.toString()) },
        tombstone(dataset = SCHEDULES, row = id.toString()),
      )
    syncController.syncChanges(changes)
    logcat.i { "Deleted schedule $id" }
  }

  // setNextDate() with reset = true
  private suspend fun resetNextDate(id: ScheduleId, date: ScheduleDate): List<LocalChange> {
    val nextDate = date.nextDate(calendar.today())
    val now = clock.now().toEpochMilliseconds()
    val existing = scheduleDao.nextDateId(id)
    val row = (existing ?: uuidGenerator(::ScheduleNextDateId)).toString()
    return buildList {
      if (existing == null) {
        add(LocalChange(SCHEDULES_NEXT_DATE, row, "schedule_id", id.toString().messageValue()))
        add(LocalChange(SCHEDULES_NEXT_DATE, row, "local_next_date", nextDate.dateValue()))
        add(LocalChange(SCHEDULES_NEXT_DATE, row, "local_next_date_ts", MessageValue.Number(now)))
        add(LocalChange(SCHEDULES_NEXT_DATE, row, TOMBSTONE, false.messageValue()))
      }
      add(LocalChange(SCHEDULES_NEXT_DATE, row, "base_next_date", nextDate.dateValue()))
      add(LocalChange(SCHEDULES_NEXT_DATE, row, "base_next_date_ts", MessageValue.Number(now)))
    }
  }

  private suspend fun checkNameIsFree(name: String?, id: ScheduleId?) {
    name ?: return
    val owner = scheduleDao.idByName(name)
    if (owner != null && owner != id) throw DuplicateScheduleNameException(name)
  }

  private suspend fun ScheduleDao.setJsonPaths(id: ScheduleId, paths: JsonPaths) =
    setJsonPaths(
      id = id,
      payee = paths.payee,
      account = paths.account,
      amount = paths.amount,
      date = paths.date,
    )
}

// normalizeScheduleName()
private fun String.normalized(): String? = trim().ifEmpty { null }

private fun List<Condition>.encode(): String =
  DbJson.encodeToString(ListSerializer(Condition.serializer()), this)

@JvmName("encodeActions")
private fun List<RuleAction>.encode(): String =
  DbJson.encodeToString(ListSerializer(RuleAction.serializer()), this)

private fun Condition.withoutType(): Condition = copy(type = null)

// Dates are stored as integers like 20261001, see toDateRepr() in loot-core
@Suppress("MagicNumber")
private fun LocalDate?.dateValue(): MessageValue =
  if (this == null) {
    Null
  } else {
    MessageValue.Number(year * 10_000L + month.number * 100L + day)
  }

// getNextDate() for either kind of date
internal fun ScheduleDate.nextDate(today: LocalDate): LocalDate? =
  when (this) {
    is Once -> date
    is Recurring -> config.nextDate(from = today)
  }

private const val TOMBSTONE = "tombstone"
