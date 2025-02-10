package actual.db.entity

import actual.budget.model.Timestamp
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// https://en.wikipedia.org/wiki/Conflict-free_replicated_data_type
// "timestamp" should be UNIQUE
@Suppress("ArrayInDataClass")
@Entity(
  tableName = "messages_crdt",
  indices = [
    Index(name = "messages_crdt_search", value = ["dataset", "row", "column", "timestamp"]),
  ],
)
data class MessageCrdtEntity(
  @PrimaryKey @ColumnInfo("id") val id: Int,
  @ColumnInfo("timestamp") val timestamp: Timestamp,
  @ColumnInfo("dataset") val dataset: String,
  @ColumnInfo("row") val row: String,
  @ColumnInfo("column") val column: String,
  @ColumnInfo("value", typeAffinity = ColumnInfo.BLOB) val value: ByteArray,
)
