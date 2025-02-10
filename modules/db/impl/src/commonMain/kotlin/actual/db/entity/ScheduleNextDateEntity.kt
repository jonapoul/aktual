package actual.db.entity

import actual.budget.model.ScheduleId
import actual.budget.model.ScheduleNextDateId
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Entity(tableName = "schedules_next_date")
data class ScheduleNextDateEntity(
  @PrimaryKey @ColumnInfo("id") val id: ScheduleNextDateId,
  @ColumnInfo("schedule_id") val scheduleId: ScheduleId?,
  @ColumnInfo("local_next_date") val localNextDate: LocalDate?,
  @ColumnInfo("local_next_date_ts") val localNextInstant: Instant?,
  @ColumnInfo("base_next_date") val baseNextDate: LocalDate?,
  @ColumnInfo("base_next_date_ts") val baseNextInstant: Instant?,
  @ColumnInfo("tombstone") val tombstone: Boolean = false,
)
