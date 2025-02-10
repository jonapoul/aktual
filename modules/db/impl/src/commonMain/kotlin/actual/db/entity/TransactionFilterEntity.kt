package actual.db.entity

import actual.budget.model.ConditionOperator
import actual.budget.model.TransactionFilterId
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_filters")
data class TransactionFilterEntity(
  @PrimaryKey @ColumnInfo("id") val id: TransactionFilterId,
  @ColumnInfo("name") val name: String?,
  @ColumnInfo("conditions") val conditions: String?,
  @ColumnInfo("conditions_op") val conditionsOp: ConditionOperator? = ConditionOperator.And,
  @ColumnInfo("tombstone") val tombstone: Boolean = false,
)
