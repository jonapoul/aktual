package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * For the format of [clock], see packages/crdt/src/crdt/timestamp.ts#serializeClock. It's a ridiculous massive JSON
 * blob
 */
@Entity(tableName = "messages_clock")
data class MessageClockEntity(
  @PrimaryKey @ColumnInfo("id") val id: Int,
  @ColumnInfo("clock") val clock: String?,
)
