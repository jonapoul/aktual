package aktual.budget.db.model

import aktual.budget.model.Timestamp
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
  tableName = "messages_crdt",
  indices =
    [Index(value = ["dataset", "row", "column", "timestamp"], name = "messages_crdt_search")],
)
data class MessagesCrdt(
  @PrimaryKey @ColumnInfo(name = "id") val id: Int,
  @ColumnInfo(name = "timestamp") val timestamp: Timestamp,
  @ColumnInfo(name = "dataset") val dataset: String,
  @ColumnInfo(name = "row") val row: String,
  @ColumnInfo(name = "column") val column: String,
  @ColumnInfo(name = "value") val value: ByteArray,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is MessagesCrdt) return false
    return id == other.id
  }

  override fun hashCode(): Int = id
}
