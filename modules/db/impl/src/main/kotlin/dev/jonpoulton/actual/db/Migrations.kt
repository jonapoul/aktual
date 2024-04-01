package dev.jonpoulton.actual.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "__migrations__")
data class MigrationEntity(
  @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = false) val id: Int,
)

@Dao
interface MigrationDao {
  @Query("SELECT * FROM __migrations__")
  suspend fun getAll(): List<MigrationEntity>
}
