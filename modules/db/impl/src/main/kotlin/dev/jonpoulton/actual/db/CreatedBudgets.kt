package dev.jonpoulton.actual.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "created_budgets")
data class CreatedBudgetEntity(
  @ColumnInfo(name = "month") @PrimaryKey(autoGenerate = false) val month: String,
)

@Dao
interface CreatedBudgetDao {
  @Query("SELECT * FROM created_budgets")
  suspend fun getAll(): List<CreatedBudgetEntity>
}
