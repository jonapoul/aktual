package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "__migrations__")
data class MigrationEntity(
  @PrimaryKey @ColumnInfo("id") val id: Int,
)
