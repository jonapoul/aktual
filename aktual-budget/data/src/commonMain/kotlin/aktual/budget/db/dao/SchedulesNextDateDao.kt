package aktual.budget.db.dao

import aktual.budget.db.converters.InstantFromLongConverter
import aktual.budget.db.model.BaseNextDate
import aktual.budget.db.model.ScheduleNextDate
import aktual.budget.model.ScheduleId
import aktual.budget.model.ScheduleNextDateId
import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.TypeConverters
import androidx.room3.Upsert
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

@Dao
@TypeConverters(InstantFromLongConverter::class)
interface SchedulesNextDateDao {
  @Query("SELECT id, base_next_date_ts FROM schedules_next_date WHERE schedule_id = :scheduleId")
  suspend fun getBaseNextDate(scheduleId: ScheduleId): BaseNextDate?

  @Query(
    "UPDATE schedules_next_date SET base_next_date = :date, base_next_date_ts = :ts WHERE id = :id"
  )
  suspend fun updateBaseDates(id: ScheduleNextDateId, date: LocalDate, ts: Instant)

  @Query(
    "UPDATE schedules_next_date SET local_next_date = :date, local_next_date_ts = :ts WHERE id = :id"
  )
  suspend fun updateLocalDates(id: ScheduleNextDateId, date: LocalDate, ts: Instant)

  @Upsert suspend fun insert(scheduleNextDate: ScheduleNextDate)
}
