package aktual.budget.db.model

import aktual.budget.model.Operator
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.json.JsonArray

@Entity(tableName = "rules")
data class Rule(
  @PrimaryKey @ColumnInfo(name = "id") val id: RuleId,
  @ColumnInfo(name = "stage") val stage: RuleStage?,
  @ColumnInfo(name = "conditions") val conditions: JsonArray?,
  @ColumnInfo(name = "actions") val actions: JsonArray?,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean? = false,
  @ColumnInfo(name = "conditions_op") val conditionsOp: Operator?,
)
