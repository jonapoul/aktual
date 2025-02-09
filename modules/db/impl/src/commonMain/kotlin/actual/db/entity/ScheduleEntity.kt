package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("rule") val rule: String?,
  @ColumnInfo("name") val name: String? = null,
  @ColumnInfo("active") val isActive: Boolean? = false,
  @ColumnInfo("completed") val isCompleted: Boolean? = false,
  @ColumnInfo("posts_transaction") val postsTransaction: Boolean? = false,
  @ColumnInfo("tombstone") val tombstone: Boolean? = false,
)
