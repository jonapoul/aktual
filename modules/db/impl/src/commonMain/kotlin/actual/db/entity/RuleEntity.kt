package actual.db.entity

import actual.db.model.Condition
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class RuleEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("stage") val stage: String?,
  @ColumnInfo("conditions") val conditions: String?,
  @ColumnInfo("actions") val actions: Int?,
  @ColumnInfo("tombstone") val tombstone: Boolean? = false,
  @ColumnInfo("conditions_op") val conditionsOp: Condition? = Condition.And,
)
