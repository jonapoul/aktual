package dev.jonpoulton.actual.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "messages_clock")
data class MessagesClockEntity(
  @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = false) val id: Int,
  @ColumnInfo(name = "clock") val clock: String,
)

@Dao
interface MessagesClockDao {
  @Query("SELECT * FROM messages_clock")
  suspend fun getAll(): List<MessagesClockEntity>
}
