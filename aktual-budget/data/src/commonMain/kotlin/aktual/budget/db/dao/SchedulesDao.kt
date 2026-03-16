package aktual.budget.db.dao

import aktual.budget.db.model.Schedule
import aktual.budget.db.model.VSchedules
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface SchedulesDao {
  @Upsert suspend fun insert(schedule: Schedule)

  @Query("UPDATE schedules SET rule = :ruleId WHERE id = :id")
  suspend fun setRule(id: ScheduleId, ruleId: RuleId)

  @Query("SELECT id FROM schedules WHERE tombstone = 0 AND name = :name")
  suspend fun getIdByName(name: String): ScheduleId?

  @Query("DELETE FROM schedules WHERE id = :id") suspend fun delete(id: ScheduleId)

  @Query("SELECT id FROM schedules WHERE rule = :ruleId")
  suspend fun getById(ruleId: RuleId): ScheduleId?

  @Query("SELECT * FROM v_schedules") suspend fun getFromVSchedules(): List<VSchedules>
}
