package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preferences")
data class PreferenceEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("value") val value: String?,
)
