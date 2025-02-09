package actual.db.entity

import actual.db.model.Condition
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_filters")
data class TransactionFilterEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("name") val name: String?,
  @ColumnInfo("conditions") val conditions: String?,
  @ColumnInfo("conditions_op") val conditionsOp: Condition? = Condition.And,
  @ColumnInfo("tombstone") val tombstone: Boolean? = false,
)
