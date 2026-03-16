package aktual.budget.db.dao

import aktual.budget.db.model.Preference
import aktual.budget.model.SyncedPrefKey
import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferencesDao {
  @Query("SELECT value FROM preferences WHERE id = :key")
  suspend fun getValue(key: SyncedPrefKey): String?

  @Upsert suspend fun setValue(preference: Preference)

  @Query("SELECT * FROM preferences") suspend fun getAll(): List<Preference>

  @Query("SELECT value FROM preferences WHERE id = :key")
  fun observe(key: SyncedPrefKey): Flow<String?>
}
