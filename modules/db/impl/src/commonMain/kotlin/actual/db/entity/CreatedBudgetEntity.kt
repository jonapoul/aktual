package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "created_budgets")
data class CreatedBudgetEntity(
  @PrimaryKey @ColumnInfo("month") val month: String,
)
