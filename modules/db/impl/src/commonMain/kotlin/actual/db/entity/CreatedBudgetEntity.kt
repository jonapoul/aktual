package actual.db.entity

import actual.budget.model.YearAndMonth
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "created_budgets")
data class CreatedBudgetEntity(
  @PrimaryKey @ColumnInfo("month") val month: YearAndMonth,
)
