package actual.db.entity

import actual.budget.model.ConditionOperator
import actual.budget.model.RuleId
import actual.db.model.Condition
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class RuleEntity(
  @PrimaryKey @ColumnInfo("id") val id: RuleId,
  @ColumnInfo("stage") val stage: String?,
  @ColumnInfo("conditions") val conditions: List<Condition>,
  @ColumnInfo("actions") val actions: Int?,
  @ColumnInfo("tombstone") val tombstone: Boolean = false,
  @ColumnInfo("conditions_op") val conditionsOp: ConditionOperator? = ConditionOperator.And,
)
