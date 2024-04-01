package dev.jonpoulton.actual.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(
    tableName = "category_groups",
    indices = [
        Index(value = ["name"], unique = true),
    ],
)
data class CategoryGroupEntity(
  @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = false) val id: String,
  @ColumnInfo(name = "name") val name: String,
  @ColumnInfo(name = "sort_order") val sortOrder: Float,
  @ColumnInfo(name = "is_income") val isIncome: Boolean = false,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean = false,
)

@Dao
interface CategoryGroupDao {
  @Query("SELECT * FROM category_groups")
  suspend fun getAll(): List<CategoryGroupEntity>
}
