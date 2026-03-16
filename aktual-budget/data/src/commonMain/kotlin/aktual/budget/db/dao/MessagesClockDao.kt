package aktual.budget.db.dao

import aktual.budget.db.model.MessagesClock
import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface MessagesClockDao {
  @Query("DELETE FROM messages_clock") suspend fun clear()

  @Upsert suspend fun insert(messagesClock: MessagesClock)

  @Query("SELECT * FROM messages_clock LIMIT 1") suspend fun getFirst(): MessagesClock?
}
