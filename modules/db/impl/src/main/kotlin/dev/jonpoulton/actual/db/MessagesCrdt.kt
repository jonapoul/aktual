package dev.jonpoulton.actual.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(
  tableName = "messages_crdt",
  indices = [
    Index(value = ["timestamp"], unique = true),
  ],
)
data class MessagesCrdtEntity(
  @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = false) val id: Int,
  @ColumnInfo(name = "timestamp") val timestamp: String,
  @ColumnInfo(name = "dataset") val dataset: String,
  @ColumnInfo(name = "row") val row: String,
  @ColumnInfo(name = "column") val column: String,
  @ColumnInfo(name = "value", typeAffinity = ColumnInfo.BLOB) val value: ByteArray,
) {
  override fun equals(other: Any?): Boolean = when {
    this === other -> true
    other !is MessagesCrdtEntity -> false
    id != other.id -> false
    timestamp != other.timestamp -> false
    dataset != other.dataset -> false
    row != other.row -> false
    column != other.column -> false
    !value.contentEquals(other.value) -> false
    else -> true
  }

  override fun hashCode(): Int {
    var result = id
    result = 31 * result + timestamp.hashCode()
    result = 31 * result + dataset.hashCode()
    result = 31 * result + row.hashCode()
    result = 31 * result + column.hashCode()
    result = 31 * result + value.contentHashCode()
    return result
  }
}

@Dao
interface MessagesCrdtDao {
  @Query("SELECT * FROM messages_crdt")
  suspend fun getAll(): List<MessagesCrdtEntity>
}
