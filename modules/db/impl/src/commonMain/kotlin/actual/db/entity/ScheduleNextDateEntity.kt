package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules_next_date")
data class ScheduleNextDateEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("schedule_id") val scheduleId: String?,
  @ColumnInfo("local_next_date") val localNextDate: Int?,
  @ColumnInfo("local_next_date_ts") val localNextDateTs: Int?,
  @ColumnInfo("base_next_date") val baseNextDate: Int?,
  @ColumnInfo("base_next_date_ts") val baseNextDateTs: Int?,
  @ColumnInfo("tombstone") val tombstone: Boolean? = false,
)
