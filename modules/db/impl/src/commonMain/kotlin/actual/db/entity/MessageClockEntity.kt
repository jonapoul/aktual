package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages_clock")
data class MessageClockEntity(
  @PrimaryKey @ColumnInfo("id") val id: Int,
  @ColumnInfo("clock") val clock: String?,
)
