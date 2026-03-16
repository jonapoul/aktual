package aktual.budget.db.model

import aktual.budget.db.converters.YearMonthFromLongConverter
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.TypeConverters
import kotlinx.datetime.YearMonth

@Entity(tableName = "reflect_budgets")
@TypeConverters(YearMonthFromLongConverter::class)
data class ReflectBudget(
  @PrimaryKey @ColumnInfo(name = "id") val id: String,
  @ColumnInfo(name = "month") val month: YearMonth?,
  @ColumnInfo(name = "category") val category: CategoryId?,
  @ColumnInfo(name = "amount") val amount: Amount?,
  @ColumnInfo(name = "carryover") val carryover: Int? = 0,
  @ColumnInfo(name = "goal") val goal: Int? = null,
  @ColumnInfo(name = "long_goal") val longGoal: Int? = null,
)
