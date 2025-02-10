package actual.db.entity

import actual.budget.model.ScheduleId
import actual.budget.model.ScheduleJsonPathIndex
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules_json_paths")
data class ScheduleJsonPathEntity(
  @PrimaryKey @ColumnInfo("schedule_id") val id: ScheduleId,
  @ColumnInfo("payee") val payee: ScheduleJsonPathIndex?,
  @ColumnInfo("account") val account: ScheduleJsonPathIndex?,
  @ColumnInfo("amount") val amount: ScheduleJsonPathIndex?,
  @ColumnInfo("date") val date: ScheduleJsonPathIndex?,
)
