package aktual.budget.db.model

import aktual.budget.db.converters.InstantFromLongConverter
import aktual.budget.model.ScheduleId
import aktual.budget.model.ScheduleNextDateId
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.TypeConverters
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

@Entity(tableName = "schedules_next_date")
@TypeConverters(InstantFromLongConverter::class)
data class ScheduleNextDate(
  @PrimaryKey @ColumnInfo(name = "id") val id: ScheduleNextDateId,
  @ColumnInfo(name = "schedule_id") val scheduleId: ScheduleId?,
  @ColumnInfo(name = "local_next_date") val localNextDate: LocalDate?,
  @ColumnInfo(name = "local_next_date_ts") val localNextDateTs: Instant?,
  @ColumnInfo(name = "base_next_date") val baseNextDate: LocalDate?,
  @ColumnInfo(name = "base_next_date_ts") val baseNextDateTs: Instant?,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean? = false,
)
