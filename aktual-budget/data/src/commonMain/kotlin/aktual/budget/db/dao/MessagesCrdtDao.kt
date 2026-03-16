package aktual.budget.db.dao

import aktual.budget.db.model.MessagesCrdt
import aktual.budget.model.Timestamp
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query

@Dao
interface MessagesCrdtDao {
  @Query("SELECT DISTINCT dataset FROM messages_crdt WHERE timestamp > :timestamp")
  suspend fun getDatasetAfterTime(timestamp: Timestamp): List<String>

  @Query("DELETE FROM messages_crdt") suspend fun clear()

  @Query(
    "SELECT timestamp FROM messages_crdt WHERE dataset = :dataset AND row = :row AND `column` = :column AND timestamp >= :timestamp"
  )
  suspend fun getTimestamp(
    dataset: String,
    row: String,
    column: String,
    timestamp: Timestamp,
  ): List<Timestamp>

  @Query("SELECT timestamp FROM messages_crdt") suspend fun getTimestamps(): List<Timestamp>

  @Insert suspend fun insert(messagesCrdt: MessagesCrdt)
}
