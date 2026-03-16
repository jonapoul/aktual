package aktual.budget.db.model

import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "schedules")
data class Schedule(
  @PrimaryKey @ColumnInfo(name = "id") val id: ScheduleId,
  @ColumnInfo(name = "rule") val rule: RuleId?,
  @ColumnInfo(name = "active") val active: Boolean? = false,
  @ColumnInfo(name = "completed") val completed: Boolean? = false,
  @ColumnInfo(name = "posts_transaction") val postsTransaction: Boolean? = false,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean? = false,
  @ColumnInfo(name = "name") val name: String? = null,
)
