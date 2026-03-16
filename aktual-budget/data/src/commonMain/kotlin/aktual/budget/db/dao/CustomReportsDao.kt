package aktual.budget.db.dao

import aktual.budget.db.model.CustomReport
import aktual.budget.model.CustomReportId
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject

@Dao
interface CustomReportsDao {
  @Insert suspend fun insert(report: CustomReport)

  @Query("SELECT * FROM custom_reports WHERE id = :id")
  suspend fun getById(id: CustomReportId): CustomReport?

  @Query("SELECT id FROM custom_reports WHERE tombstone = 0 AND name = :name LIMIT 1")
  suspend fun getIdByName(name: String): CustomReportId?

  @Query("SELECT id FROM custom_reports") suspend fun getIds(): List<CustomReportId>

  @Query("SELECT metadata FROM custom_reports WHERE id = :id")
  fun observeMetadataById(id: CustomReportId): Flow<JsonObject?>

  @Query("DELETE FROM custom_reports WHERE id = :id") suspend fun delete(id: CustomReportId)
}
