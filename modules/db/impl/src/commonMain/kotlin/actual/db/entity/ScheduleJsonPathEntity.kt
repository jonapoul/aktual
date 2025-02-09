package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules_json_paths")
data class ScheduleJsonPathEntity(
  @PrimaryKey @ColumnInfo("schedule_id") val id: String,
  @ColumnInfo("payee") val payee: String?,
  @ColumnInfo("account") val account: String?,
  @ColumnInfo("amount") val amount: String?,
  @ColumnInfo("date") val date: String?,
)
