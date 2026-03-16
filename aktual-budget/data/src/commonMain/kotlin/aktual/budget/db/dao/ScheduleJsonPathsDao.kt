package aktual.budget.db.dao

import aktual.budget.db.model.ScheduleJsonPath
import androidx.room3.Dao
import androidx.room3.Upsert

@Dao
interface ScheduleJsonPathsDao {
  @Upsert suspend fun insert(scheduleJsonPath: ScheduleJsonPath)
}
