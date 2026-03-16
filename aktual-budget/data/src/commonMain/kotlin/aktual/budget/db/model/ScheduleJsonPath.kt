package aktual.budget.db.model

import aktual.budget.model.ScheduleId
import aktual.budget.model.ScheduleJsonPathIndex
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "schedules_json_paths")
data class ScheduleJsonPath(
  @PrimaryKey @ColumnInfo(name = "schedule_id") val scheduleId: ScheduleId,
  @ColumnInfo(name = "payee") val payee: ScheduleJsonPathIndex?,
  @ColumnInfo(name = "account") val account: ScheduleJsonPathIndex?,
  @ColumnInfo(name = "amount") val amount: ScheduleJsonPathIndex?,
  @ColumnInfo(name = "date") val date: ScheduleJsonPathIndex?,
)
