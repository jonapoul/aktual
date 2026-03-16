package aktual.budget.db.model

import aktual.budget.model.Operator
import aktual.budget.model.TransactionFilterId
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.json.JsonArray

@Entity(tableName = "transaction_filters")
data class TransactionFilter(
  @PrimaryKey @ColumnInfo(name = "id") val id: TransactionFilterId,
  @ColumnInfo(name = "name") val name: String?,
  @ColumnInfo(name = "conditions") val conditions: JsonArray?,
  @ColumnInfo(name = "conditions_op") val conditionsOp: Operator?,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean? = false,
)
