package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "banks")
data class BankEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("bank_id") val bankId: String?,
  @ColumnInfo("name") val name: String?,
  @ColumnInfo("tombstone") val tombstone: Boolean? = false,
)
