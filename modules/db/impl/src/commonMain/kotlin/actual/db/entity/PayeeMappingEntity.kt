package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payee_mapping")
data class PayeeMappingEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("targetId") val targetId: String?,
)
