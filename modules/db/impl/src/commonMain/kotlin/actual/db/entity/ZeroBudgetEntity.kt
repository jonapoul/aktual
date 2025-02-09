package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zero_budgets")
data class ZeroBudgetEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("month") val month: Int?,
  @ColumnInfo("category") val category: String?,
  @ColumnInfo("amount") val amount: Int? = 0,
  @ColumnInfo("carryover") val carryover: Int? = 0,
  @ColumnInfo("goal") val goal: Int? = null,
  @ColumnInfo("long_goal") val longGoal: Int? = null,
)
