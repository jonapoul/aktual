package aktual.budget.db.model

import aktual.budget.db.converters.InstantFromLongConverter
import aktual.budget.model.ScheduleNextDateId
import androidx.room3.ColumnInfo
import androidx.room3.TypeConverters
import kotlin.time.Instant

@TypeConverters(InstantFromLongConverter::class)
data class BaseNextDate(
  @ColumnInfo(name = "id") val id: ScheduleNextDateId,
  @ColumnInfo(name = "base_next_date_ts") val baseNextDateTs: Instant?,
)
