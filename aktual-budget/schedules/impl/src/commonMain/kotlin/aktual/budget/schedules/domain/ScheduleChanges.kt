package aktual.budget.schedules.domain

import aktual.budget.db.dao.DatabaseTables.SCHEDULES
import aktual.budget.db.dao.DatabaseTables.SCHEDULES_NEXT_DATE
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.db.schedules.GetAllActive
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.RecurConfig
import aktual.budget.model.ScheduleId
import aktual.budget.model.localChange
import aktual.budget.model.messageValue
import aktual.log
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

// setNextDate() without reset in packages/loot-core/src/server/schedules/app.ts. The local next
// date is used while its timestamp matches the base one
internal suspend fun ScheduleDao.localNextDateChanges(
  id: ScheduleId,
  date: LocalDate,
): List<LocalChange> {
  val existing = baseNextDate(id) ?: return emptyList()
  val row = existing.id.toString()
  val timestamp = existing.base_next_date_ts?.toEpochMilliseconds()
  return listOf(
    LocalChange(SCHEDULES_NEXT_DATE, row, "local_next_date", date.messageValue()),
    LocalChange(
      SCHEDULES_NEXT_DATE,
      row,
      "local_next_date_ts",
      timestamp?.let(MessageValue::Number) ?: MessageValue.Null,
    ),
  )
}

internal fun completedChange(id: ScheduleId, completed: Boolean): LocalChange =
  localChange(SCHEDULES, id.toString(), "completed", completed)

internal sealed interface ScheduleDate {
  data class Once(val date: LocalDate) : ScheduleDate

  data class Recurring(val config: RecurConfig) : ScheduleDate
}

// _date is either a plain "YYYY-MM-DD" or a RecurConfig JSON object. A corrupted one gives null, as
// upstream skips a schedule when it can't find the rule
internal fun GetAllActive.parseDate(): ScheduleDate? {
  val raw = _date ?: return null
  return runCatching {
    when (val element = Json.parseToJsonElement(raw)) {
      is JsonObject ->
        ScheduleDate.Recurring(Json.decodeFromJsonElement(RecurConfig.serializer(), element))
      is JsonNull,
      is JsonArray -> null
      is JsonPrimitive -> ScheduleDate.Once(LocalDate.parse(element.content))
    }
  }
    .onFailure { log.w(it) { "Couldn't parse date of schedule $id: $raw" } }
    .getOrNull()
}
