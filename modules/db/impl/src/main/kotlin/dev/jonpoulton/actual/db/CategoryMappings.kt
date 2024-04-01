package dev.jonpoulton.actual.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "category_mapping")
data class CategoryMappingEntity(
  @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = false) val id: String,
  @ColumnInfo(name = "transferId") val transferId: String,
)

@Dao
interface CategoryMappingDao {
  @Query("SELECT * FROM category_mapping")
  suspend fun getAll(): List<CategoryMappingEntity>
}
