package aktual.budget.db.dao

import aktual.budget.db.model.Dashboard
import aktual.budget.db.model.DashboardPositionAndSize
import aktual.budget.model.WidgetId
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query

@Dao
interface DashboardDao {
  @Insert suspend fun insert(dashboard: Dashboard)

  @Query("SELECT id FROM dashboard WHERE tombstone = 0 ORDER BY y DESC, x DESC")
  suspend fun getIds(): List<WidgetId>

  @Query("DELETE FROM dashboard WHERE id = :id") suspend fun delete(id: WidgetId)

  @Query("SELECT x, y, width, height FROM dashboard WHERE tombstone = 0 ORDER BY y DESC, x DESC")
  suspend fun getPositionAndSize(): List<DashboardPositionAndSize>
}
