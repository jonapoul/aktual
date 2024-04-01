package dev.jonpoulton.actual.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "categories")
data class CategoryEntity(
  @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = false) val id: String,
  @ColumnInfo(name = "name") val name: String,
  @ColumnInfo(name = "cat_group") val catGroup: String,
  @ColumnInfo(name = "sort_order") val sortOrder: Float,
  @ColumnInfo(name = "is_income") val isIncome: Boolean = false,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean = false,
)

@Dao
interface CategoryDao {
  @Query("SELECT * FROM categories")
  suspend fun getAll(): List<CategoryEntity>
}
