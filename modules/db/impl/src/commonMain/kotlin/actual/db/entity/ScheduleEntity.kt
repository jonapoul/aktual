package actual.db.entity

import actual.budget.model.RuleId
import actual.budget.model.ScheduleId
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "schedules",
  foreignKeys = [
    ForeignKey(RuleEntity::class, parentColumns = ["id"], childColumns = ["rule"]),
  ],
  indices = [
    Index(value = ["rule"]),
  ],
)
data class ScheduleEntity(
  @PrimaryKey @ColumnInfo("id") val id: ScheduleId,
  @ColumnInfo("rule") val rule: RuleId?,
  @ColumnInfo("name") val name: String? = null,
  @ColumnInfo("active") val isActive: Boolean? = false,
  @ColumnInfo("completed") val isCompleted: Boolean? = false,
  @ColumnInfo("posts_transaction") val postsTransaction: Boolean? = false,
  @ColumnInfo("tombstone") val tombstone: Boolean = false,
)
