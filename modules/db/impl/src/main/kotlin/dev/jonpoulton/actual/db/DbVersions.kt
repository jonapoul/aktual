package dev.jonpoulton.actual.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "db_version")
data class DbVersionEntity(
  @ColumnInfo(name = "version") @PrimaryKey(autoGenerate = false) val version: Int,
)

@Dao
interface DbVersionDao {
  @Query("SELECT * FROM db_version")
  suspend fun getAll(): List<DbVersionEntity>
}
